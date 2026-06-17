(ns lodestar.gatepolicy
  (:require [clojure.string :as str])
  (:import [java.security MessageDigest]))

^{:line 14 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (defrecord Tenant [tid host port])

(defn tenant-tid [r] (:tid r))

(defn tenant-host [r] (:host r))

(defn tenant-port [r] (:port r))

^{:line 16 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (defrecord Bucket [tokens ts ok])

(defn bucket-tokens [r] (:tokens r))

(defn bucket-ts [r] (:ts r))

(defn bucket-ok [r] (:ok r))

^{:line 19 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (defn- ^String hex-byte [b]
  ^{:line 20 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (let [v ^{:line 20 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (bit-and b 255)
   h "0123456789abcdef"]
  ^{:line 21 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (str ^{:line 21 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (subs h ^{:line 21 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (quot v 16) ^{:line 21 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (+ ^{:line 21 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (quot v 16) 1)) ^{:line 22 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (subs h ^{:line 22 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (rem v 16) ^{:line 22 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (+ ^{:line 22 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (rem v 16) 1)))))

^{:line 24 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (defn ^String sha256-hex [^String s]
  ^{:line 25 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (let [md ^{:line 25 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (MessageDigest/getInstance "SHA-256")
   bs ^{:line 25 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (.digest md ^{:line 25 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (.getBytes s "UTF-8"))]
  ^{:line 27 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (reduce ^{:line 27 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (fn [acc b] ^{:line 27 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (str acc ^{:line 27 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (hex-byte b))) "" ^{:line 27 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (vec bs))))

^{:line 30 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (defn ^String bearer-token [^String auth]
  ^{:line 31 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (if ^{:line 31 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (and ^{:line 31 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (some? auth) ^{:line 31 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (str/starts-with? auth "Bearer ")) ^{:line 31 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (subs auth 7) ""))

^{:line 35 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (defn parse-tenant [^String tid cfg]
  ^{:line 36 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (let [host ^{:line 36 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (:coordinator-host cfg)
   port ^{:line 36 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (:coordinator-port cfg)]
  ^{:line 37 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (if ^{:line 37 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (and ^{:line 37 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (some? port)) ^{:line 38 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (->Tenant tid ^{:line 38 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (if ^{:line 38 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (some? host) host "127.0.0.1") port) nil)))

^{:line 42 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (defn token->tenant [by-hash ^String token]
  ^{:line 43 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (if ^{:line 43 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (and ^{:line 43 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (some? token) ^{:line 43 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (not ^{:line 43 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (= token ""))) ^{:line 44 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (get by-hash ^{:line 44 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (sha256-hex token)) nil))

^{:line 48 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (defn ^Bucket bucket-step [b now rate burst]
  ^{:line 49 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (let [bk ^{:line 49 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (if ^{:line 49 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (some? b) b ^{:line 49 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (->Bucket burst now true))
   elapsed ^{:line 49 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (max 0.0 ^{:line 49 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (/ ^{:line 49 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (- now ^{:line 49 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (:ts bk)) 1000000000.0))
   refilled ^{:line 49 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (min burst ^{:line 49 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (+ ^{:line 49 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (:tokens bk) ^{:line 49 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (* elapsed rate)))]
  ^{:line 52 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (if ^{:line 52 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (>= refilled 1.0) ^{:line 53 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (->Bucket ^{:line 53 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (- refilled 1.0) now true) ^{:line 54 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (->Bucket refilled now false))))

^{:line 58 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (defn ^Boolean valid-op? [parsed]
  ^{:line 59 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (and ^{:line 59 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (map? parsed) ^{:line 59 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (keyword? ^{:line 59 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (:op parsed))))

^{:line 62 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (defn ^String coord-status [^String resp]
  ^{:line 63 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (cond
  ^{:line 64 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (nil? resp) "no-response"
  ^{:line 65 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (str/includes? resp ":error") "error"
  ^{:line 66 :file "/home/tom/code/lodestar/src/lodestar/gatepolicy.bclj"} (str/includes? resp ":conflict") "conflict"
  :else "ok"))
