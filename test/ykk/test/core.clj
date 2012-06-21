(ns ykk.test.core
  (:require [clojure.zip :as zip]
            [ykk.core :as ykk]
            :reload-all)
  (:use expectations))

(def % partial)

(let [zipper (zip/vector-zip [:a [:b :c [:d]]])]
  (expect
    [:a [:c [:d]]]
    (ykk/remove (% = :b) zipper))
  (expect
    [:a [:b :c]]
    (ykk/remove #(and (coll? %) (= (count %) 1)) zipper))
  (expect
    [:a]
    (ykk/remove #(and (coll? %) (= (count %) 3)) zipper)))

(let [zipper (zip/vector-zip [0 [1 [2] 3] 4])]
  (expect
    [0 [2 [2] 4] 4]
    (ykk/leaf-map inc odd? zipper)))

(let [zipper (zip/vector-zip [[:one] [:two] [:red] [:blue]])]
  (expect
    [[:one :fish] [:two :fish] [:red :fish] [:blue :fish]]
    (ykk/map #(conj % :fish)
           #(and (coll? %) (every? keyword? %))
           zipper)))

(let [zipper (zip/vector-zip [:hay [:hay :hay] [:hay :hay [:hay :needle]]])]
  (expect
    :needle
    (zip/node (ykk/find (% = :needle) zipper)))
  (expect
    [[] [[:needle]]]
    (ykk/filter-leaves (% = :needle) zipper)))

(defn -main [& args]
  (System/exit 0))
