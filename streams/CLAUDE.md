# streams/ — lossless capture + tiered distillation

The stream layer the operating manual describes. Two directories:

- `streams/raw/` — **lossless transmission events**: full session transcripts
  (Claude Code JSONL), dictated thoughts, captured conversations. **Local-only,
  gitignored** — raw transcripts carry everything (private context, tool
  output); the repo publishes projections, not the source signal. Files:
  `YYYY-MM-DD-<slug>.<session-id>.jsonl`. A copy is a snapshot — live sessions
  keep appending; re-snapshot at session end.
- `streams/distillations/` — **committed tiered compressions** of raw streams.
  Tier 1 = one session → decisions, principles, spawned threads, artifacts,
  with `@thread-id` links so the claim graph and the narrative cross-reference.
  Files: `YYYY-MM-DD-<slug>.tier1.md`.

Provenance contract: every distillation names its raw source(s) and the tern
thread minted for the session (`stream thread`), which carries `relates_to`
edges to every thread the conversation spawned. Chain: utterance → distillation
→ stream thread → spawned thread → outcome claim → commit. Queryable end to end.

Mining (retry loops, verb votes, doc re-reads) is `tern-mine`'s job, not this
layer's — raw here is its input corpus.
