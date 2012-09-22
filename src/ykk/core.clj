(ns ykk.core
  (:require [clojure.zip :as zip]))

(defn- andf
  "Same semantics as clojure.core/and (in particular short-circuiting), but is a
  higher-order function instead of a macro."
  [& fns]
  (fn [& args]
    (loop [fns fns]
      (if-let [f (first fns)]
        (if (apply f args)
          (recur (rest fns))
          false)
        ; else (fns is empty)
        true))))

(defn- orf
  "Same semantics as clojure.core/or (in particular short-circuiting), but is a
  higher-order function instead of a macro."
  [& fns]
  (fn [& args]
    (loop [fns fns]
      (if-let [f (first fns)]
        (if (apply f args)
          true
          (recur (rest fns)))
        ; else (fns is empty)
        false))))

(def branch? zip/branch?)
(def leaf?
  "The opposite of `clojure.zip/branch?`."
  (complement branch?))

(defn find*
  "This is the underlying implementation of find-{leaf, branch}. The first loc
  satisfying either one of or both loc-pred and node-pred, depending on the
  value of the or kwarg, is returned. Note the implicit call to clojure.zip/node
  that precedes node-pred."
  ([type-pred node-pred zipper]
   (find* type-pred (constantly true) node-pred zipper))
  ([type-pred loc-pred node-pred zipper & {:keys [or]}]
   (let [user-pred-op (if or orf andf)
         pred (andf type-pred
                    (user-pred-op loc-pred (comp node-pred zip/node)))]
     (loop [loc zipper]
       (if (zip/end? loc)
         nil
         (if (pred loc)
           loc
           (recur (zip/next loc))))))))

(defn find-leaf
  "Does a depth-first traversal of zipper starting from its present location,
  and returns the first leaf satisfying (by default all of) the passed
  predicates. The implicit `and` of the predicates may be changed to an `or` by
  passing a truthy value for the :or kwarg."
  ([node-pred zipper]
   (find* leaf? node-pred zipper))
  ([loc-pred node-pred zipper & {:keys [or]}]
   (find* leaf? loc-pred node-pred zipper :or or)))

(defn find-branch
  "Does a depth-first traversal of zipper starting from its present location,
  and returns the first branch satisfying (by default all of) the passed
  predicates. The implicit `and` of the predicates may be changed to an `or` by
  passing a truthy value for the :or kwarg."
  ([node-pred zipper]
   (find* branch? node-pred zipper))
  ([loc-pred node-pred zipper & {:keys [or]}]
   (find* branch? loc-pred node-pred zipper :or or)))

(defn- filter*
  ([type-pred node-pred zipper]
   (filter* type-pred (constantly true) node-pred zipper))
  ([type-pred loc-pred node-pred zipper & {:keys [or]}]
   (let [user-pred-op (if or orf andf)
         pred (orf type-pred
                   (user-pred-op loc-pred (comp node-pred zip/node)))]
     (loop [loc zipper]
       (if (zip/end? loc)
         (zip/root loc)
         (if (pred loc)
           (recur (zip/next loc))
           (recur (zip/remove loc))))))))

(defn filter-leaves
  "Does a depth-first traversal of zipper starting from its present location,
  and returns the root of a new zipper that includes only those leaves that
  satisfy (by default all of) the passed predicates. The implicit `and` of the
  predicates may be changed to an `or` by passing a truthy value for the :or
  kwarg. Note that all branches pass implicitly."
  ([node-pred zipper]
   (filter* branch? node-pred zipper))
  ([loc-pred node-pred zipper & {:keys [or]}]
   (filter* branch? loc-pred node-pred zipper :or or)))

(defn filter-branches
  "Does a depth-first traversal of zipper starting from its present location,
  and returns either the root of a new zipper that includes only those branches
  that satisfy (by default all of) the passed predicates, or nil if the root
  node of the input zipper happens to be traversed and fails to be selected
  by the filtering predicates. The implicit `and` of the predicates may be
  changed to an `or` by passing a truthy value for the :or kwarg. Note that all
  leaves pass implicitly, so they are removed iff their parent branch is."
  ([node-pred zipper]
   (filter-branches (constantly true) node-pred zipper))
  ([loc-pred node-pred zipper & {:keys [or]}]
   (try
     (filter* leaf? loc-pred node-pred zipper :or or)
     (catch Exception e
       (if (= (.getMessage e) "Remove at top")
         nil
         (throw e))))))

(defn remove-leaves
  "Does a depth-first traversal of zipper starting from its present location,
  and returns the root of a new zipper with those leaves removed that satisfy
  (by default all of) the passed predicates. The implicit `and` of the
  predicates may be changed to an `or` by passing a truthy value for the :or
  kwarg. Note that all branches fail implicitly, so they are kept."
  ([node-pred zipper]
   (filter* branch? (constantly false) (complement node-pred) zipper :or true))
  ([loc-pred node-pred zipper & {:keys [or]}]
   (filter* branch?
            (complement loc-pred)
            (complement node-pred)
            zipper
            :or (not or))))

(defn remove-branches
  "Does a depth-first traversal of zipper starting from its present location,
  and returns either the root of a new zipper with those branches removed that
  satisfy (by default all of) the passed predicates, or nil if the root node of
  input zipper happens to be traversed and passes the removal criteria. The
  implicit `and` of the predicates may be changed to an `or` by passing a truthy
  value for the :or kwarg. Note that all leaves fail implicitly, so they are
  removed iff their parent branch is."
  ([node-pred zipper]
   (remove-branches (constantly true) node-pred zipper))
  ([loc-pred node-pred zipper & {:keys [or]}]
   (try
     (filter* leaf?
              (complement loc-pred)
              (complement node-pred)
              zipper
              :or (not or))
     (catch Exception e
       (if (= (.getMessage e) "Remove at top")
         nil
         (throw e))))))

(defn map-locs*
  ([f type-pred zipper]
   (map-locs* f type-pred (constantly true) zipper))
  ([f type-pred pred zipper]
   (let [pred' (andf type-pred pred)]
     (loop [loc zipper]
       (if (zip/end? loc)
         (zip/root loc)
         (if (pred' loc)
           (recur (zip/next (f loc)))
           (recur (zip/next loc))))))))

(defn- map*
  ([f type-pred zipper]
   (map* f type-pred (constantly true) (constantly true) zipper))
  ([f type-pred node-pred zipper]
   (map* f type-pred (constantly true) node-pred zipper))
  ([f type-pred loc-pred node-pred zipper & {:keys [or]}]
   (let [user-pred-op (if or orf andf)
         pred (andf type-pred
                     (user-pred-op loc-pred (comp node-pred zip/node)))]
     (loop [loc zipper]
       (if (zip/end? loc)
         (zip/root loc)
         (if (pred loc)
           (recur (zip/next (zip/edit loc f)))
           (recur (zip/next loc))))))))

(defn map-leaves
  "Does a depth-first traversal of zipper starting from its present location,
  and returns the root of a new zipper obtained by, during the traversal,
  applying f to the node of every leaf that satisfies (by default all of) the
  passed predicates (if any). The implicit `and` of the predicates may be
  changed to an `or` by passing a truthy value for the :or kwarg."
  ([f zipper]
   (map* f leaf? zipper))
  ([f node-pred zipper]
   (map* f leaf? node-pred zipper))
  ([f loc-pred node-pred zipper & {:keys [or]}]
   (map* f leaf? loc-pred node-pred zipper :or or)))

(defn map-branches
  "Does a depth-first traversal of zipper starting from its present location,
  and returns the root of a new zipper obtained by, during the traversal,
  applying f to the node of every branch that satisfies (by default all of) the
  passed predicates (if any). The implicit `and` of the predicates may be
  changed to an `or` by passing a truthy value for the :or kwarg."
  ([f zipper]
   (map* f branch? zipper))
  ([f node-pred zipper]
   (map* f branch? node-pred zipper))
  ([f loc-pred node-pred zipper & {:keys [or]}]
   (map* f branch? loc-pred node-pred zipper :or or)))
