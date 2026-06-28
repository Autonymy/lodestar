# The incremental-aggregate primitive — quorum + budget, one fold

> Lives in `~/code/lodestar/cli/coord.clj`. Roadmap tier **F** (quorum) + **G**
> (budget), decision **6**: _"EVERYTHING COUNTABLE IS A FOLD OVER AN APPEND-ONLY
> LOG, NEVER A MUTATED CELL."_ Thread `019f100f` (Foundation), Part: quorum.

## The thesis

Coordination has two halves. **Exclusion** answers _"who may act?"_ by REJECTING
the second writer (locks, leases, OCC). **Completion** answers _"are we done /
how much have we spent?"_ — and it needs none of that. It ACCEPTS every writer
and DERIVES the answer by folding the append-only log at read time.

So completion is the **dual** of mutual exclusion, and it is structurally one
thing — an incremental aggregate — seen through two reducers:

| use | reducer | gate |
|---|---|---|
| **quorum** (K-of-N barrier) | `count-distinct(key)` | `>= K` → fired |
| **budget** (spend ceiling) | `Σ(value)` | `< cap` → admit |

Both reducers are **commutative** and **idempotent** (set-union collapses a
double-reported worker; `+` over write-once `@run`/`@charge` subjects is stable),
so retry, double-report, and racing writers all converge with **zero write-time
coordination**. Each fold is a pure, recomputable function of a log prefix —
never a cached cell that can silently diverge from its source (the two-budgets /
mutated-`budget_spent` bug, killed at the root). The earliest-cid total order that
other derivations need to agree is not even required here: `+` and `∪` are
order-independent.

## The API (`lodestar.coord`)

A **reducer** is `{:init :step :final}`. The two production reducers — the only
two coordination has ever needed:

```clojure
distinct-reducer   ; quorum: union row's 1st binding into a SET (key seen twice = once)
sum-reducer        ; budget: Σ the numeric 2nd binding of each row
```

The fold and its convenience folds:

```clojure
(agg-rows port project body)         ; rows a Datalog BODY binds, projected onto PROJECT
(aggregate port project body reducer); the primitive: fold REDUCER over those rows

(distinct-of    port [key] body)     ; the SET of distinct keys
(count-distinct port [key] body)     ; |set|  — the quorum left side
(sum-of         port [key val] body) ; Σ of val — the budget/spend fold
(quorum-met?    port k [key] body)   ; (>= (count-distinct …) k) — the barrier gate

;; the row seam — fold rows you already hold (e.g. after a client-side scope the
;; scan body can't express, like an entity-id PREFIX):
(reduce-rows reducer rows)
(sum-rows rows)          ; Σ-reduce a [key val] row-seq
(distinct-rows rows)     ; set-reduce a [key] row-seq
```

### The projection asymmetry (don't skip this)

The scan engine's derived head is a **set of tuples**. That is exactly what
quorum wants (`distinct-of` projects `[key]` so equal keys collapse) and exactly
what budget must avoid (`sum-of` projects **`[key val]`** so two equal-valued
addends — two equal-cost runs — stay distinct instead of deduping to one and
**under-counting**). Same engine behavior; opposite need; that is why the two
reducers project different arities. A value-only Σ is a silent under-count bug.

## Adoptions (wired)

- **dispatch fan-in** — `cli/lodestar-map.clj`: the K-of-N fan-out barrier is
  `distinct-of` over `@done:* done_worker`. `complete? := count-distinct >= K`.
- **swarm gate** — `cli/lodestar-listen.clj`:
  - `spent-sum` = `coord/sum-rows` over `@run:* cost_usd` (scoped by an `@run:`
    prefix through the row seam) — the budget Σ.
  - `live-drivers` = `coord/count-distinct` over `driver` subjects — the
    concurrency ceiling **is a quorum over live drivers** (replacing the
    `@swarm-slot` semaphore).

Both reducers, both sites: budget and quorum now share **one** substrate.

## Boundaries (open, per the roadmap red-team)

- **F ↔ H (monotonicity vs retraction).** `quorum-met?` is monotone only while
  completion predicates (`done_worker`) are **irretractable**. First-class
  retraction (tier H) would let a withdrawn DONE decrement the count and **un-fire**
  a barrier agents already acted on. Until H resolves this, treat completion
  claims as append-only.
- **Single cid allocator.** The folds here don't need the total order, but any
  derivation that elects does; sharding id-allocation breaks `earliest-cid` as a
  total order. Out of scope for this primitive.

## Test

`bb cli/tests/aggregate-test.clj [port]` — live `:7977`, 8/8: count-distinct
collapse, quorum fire/wait at K, idempotent re-report, Σ with an equal-valued
addend (the dedup trap), the budget gate, and the row seam.
