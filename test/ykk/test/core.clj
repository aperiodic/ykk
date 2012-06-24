(ns ykk.test.core
  (:require [clojure.zip :as zip]
            [ykk.core :as ykk]
            :reload-all)
  (:use expectations))

(def % partial)

(let [haystack (zip/vector-zip [:hay :hay [:needle :hay] :hay :hay])]
  (expect
    :needle
    (zip/node (ykk/find-leaf (% = :needle) haystack)))
  (expect
    [:needle :hay]
    (zip/node (ykk/find-branch #((set %) :needle) haystack)))
  (expect
    :needle
    (-> (ykk/find-leaf (comp empty? zip/lefts) (% = :needle) haystack)
      zip/node))
  (expect
    :hay
    (-> (ykk/find-leaf (comp empty? zip/lefts) (% = :needle) haystack :or true)
      zip/node)))

(let [numbers (zip/vector-zip [0 [1 [2] 3] 4])]
  (expect
    [0 [[2]] 4]
    (ykk/filter-leaves even? numbers))
  (expect
    [[1 [] 3]]
    (ykk/remove-leaves even? numbers))
  (expect
    [0 [1 3] 4]
    (ykk/remove-branches #(= (count %) 1) numbers))
  (expect
    nil
    (ykk/filter-branches (comp even? count) numbers))
  (expect
    nil
    (ykk/remove-branches (comp odd? count) numbers))
  (expect
    [[[] 3]]
    (ykk/filter-leaves (comp empty? zip/rights) odd? numbers))
  (expect
    [[1 [2] 3] 4]
    (ykk/filter-leaves (comp empty? zip/rights) odd? numbers :or true))
  (expect
    [0 [1 [] 3]]
    (ykk/remove-leaves (comp empty? zip/rights) even? numbers))
  (expect
    [[1 []]]
    (ykk/remove-leaves (comp empty? zip/rights) even? numbers :or true))
  (expect
    [0 [1 [] 3]]
    (ykk/remove-leaves #(empty? (zip/right %)) even? numbers))
  (expect
    [1 [2 [3] 4] 5]
    (ykk/map-leaves inc numbers))
  (expect
    [0 [2 [2] 4] 4]
    (ykk/map-leaves inc odd? numbers))
  (expect
    [0 [1 [2] 4] 4]
    (ykk/map-leaves inc (comp empty? zip/rights) odd? numbers))
  (expect
    [0 [2 [3] 4] 5]
    (ykk/map-leaves inc (comp empty? zip/rights) odd? numbers :or true)))

(let [zipper (zip/vector-zip [[:one] [:two] [:red] [:blue]])]
  (expect
    [[:one :fish] [:two :fish] [:red :fish] [:blue :fish]]
    (ykk/map-branches #(conj % :fish) #(every? keyword? %) zipper)))

(defn -main [& args]
  (System/exit 0))
