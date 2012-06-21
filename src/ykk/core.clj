(ns ykk.core
  (:refer-clojure :exclude [find map remove filter])
  (:require [clojure.zip :as zip]))

(def leaf?
  "The opposite of 'zip/branch?'."
  (complement zip/branch?))

(defn remove
  "Remove from zipper all locs whose nodes satisfy the selector."
  [selector zipper]
  (loop [loc zipper]
    (if (zip/end? loc)
      (zip/root loc)
      (if (selector (zip/node loc))
        (recur (zip/remove loc))
        (recur (zip/next loc))))))

(defn filter-leaves
  "Keep only those leaves whose nodes satisfy selector, without changing the
  structure of the zipper (i.e. modifying any branches)."
  [selector zipper]
  (loop [loc zipper]
    (if (zip/end? loc)
      (zip/root loc)
      (if (or (zip/branch? loc) (selector (zip/node loc)))
        (recur (zip/next loc))
        (recur (zip/remove loc))))))

(defn find
  "Return the first loc in zipper whose node satisfies selector."
  [selector zipper]
  (loop [loc zipper]
    (if (zip/end? loc)
      nil
      (if (selector (zip/node loc))
        loc
        (recur (zip/next loc))))))

(defn map
  "Transform the zipper by applying f to every node (branch and leaf), or only
  those nodes that satisfy the selector."
  ([f zipper]
   (map f (constantly true) zipper))
  ([f selector zipper]
   (loop [loc zipper]
     (if (zip/end? loc)
       (zip/root loc)
       (if (selector (zip/node loc))
         (recur (zip/next (zip/edit loc f)))
         (recur (zip/next loc)))))))

(defn leaf-map
  "Transform the zipper by applying f to every leaf, or only those leaves
  that satisfy the selector."
  ([f zipper]
   (leaf-map f (constantly true) zipper))
  ([f selector zipper]
   (loop [loc zipper]
     (if (zip/end? loc)
       (zip/root loc)
       (if (and (leaf? loc) (selector (zip/node loc)))
         (recur (zip/next (zip/edit loc f)))
         (recur (zip/next loc)))))))
