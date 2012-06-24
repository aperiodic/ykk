(ns examples
  (:require [clojure.zip :as zip]
            [ykk.core :as ykk] :reload-all))

;; The find-{branch, leaf} functions
;; ---------------------------------

(def haystack (zip/vector-zip [:hay :hay [:needle :hay] :hay :hay]))
(zip/node )
(-> (ykk/find-leaf (partial = :needle) haystack) zip/node)
; => :needle

(-> (ykk/find-branch #((set %) :needle) haystack) zip/node)
; => [:needle: hay]

; note the implicit `and` of the loc and node preds by default
(-> (ykk/find-leaf (comp empty? zip/lefts) (partial = :needle) haystack)
  zip/node)
; => :needle

; the implicit `and` can be changed to an `or` by setting the :or kwarg truthy
(-> (ykk/find-leaf (comp empty? zip/lefts) (partial = :needle) haystack :or true)
  zip/node)
; => :hay

;; The {filter, remove}-{branches, leaves} functions
;; -------------------------------------------------

(def numbers (zip/vector-zip [0 [1 [2] 3] 4]))

(ykk/filter-leaves even? numbers)
; => [0 [[2]] 4]

(ykk/remove-leaves even? numbers)
; => [[1 [] 3]]

(ykk/remove-leaves odd? numbers)
; => [0 [[2]] 4]

(ykk/remove-branches #(= (count %) 1) numbers)
; => [0 [1 3] 4]

;; note that the root node fails this predicate
(ykk/filter-branches (comp even? count) numbers)
; => nil

;; note again the implicit `and` by default
(ykk/filter-leaves (comp empty? zip/rights) odd? numbers)
; => [[[] 3]]

;; the implicit `and` can be an `or` by setting the :or kwarg truthy
(ykk/filter-leaves (comp empty? zip/rights) odd? numbers :or true)
; => [[1 [2] 3] 4]

;; same goes for remove-{branches, leaves}: `and` by default, `or` with kwarg
(ykk/remove-leaves (comp empty? zip/rights) even? numbers)
; => [0 [1 [] 3]]

(ykk/remove-leaves (comp empty? zip/rights) even? numbers :or true)
; => [[1 []]]

;; The map-{branches, leaves} functions
;; ------------------------------------

(def numbers (zip/vector-zip [0 [1 [2] 3] 4]))

(ykk/map-leaves inc numbers)
; => [1 [2 [3] 4] 5]

(ykk/map-leaves inc odd? numbers)
; => [0 [2 [2] 4] 4]

;; again, `and` by default, `or` with kwarg
(ykk/map-leaves inc (comp empty? zip/rights) odd? numbers)
; => [0 [1 [2] 4] 4] ; only '3' is incremented

(ykk/map-leaves inc (comp empty? zip/rights) odd? numbers :or true)
; => [0 [2 [3] 4] 5] ; everything but '0' is incremented

(def fish (zip/vector-zip [[:one] [:two] [:red] [:blue]]))

(ykk/map-branches #(conj % :fish) #(every? keyword? %) fish)
; => [[:one :fish], [:two :fish], [:red :fish], [:blue fish]]

;; Infinite loop!!
;; f is applied *during* the traversal, so turning leaves into branches will
;; make the traversal a sisyphean task.
(ykk/map-leaves #(conj (vector %) :fish)
                (zip/vector-zip [:one :two :red :blue]))
