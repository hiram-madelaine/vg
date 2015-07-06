(ns ^:figwheel-always testvg.bonus
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [reagent.core :as reagent :refer [atom]]
    [cljs.core.async :refer [chan put! <! timeout alts!]]
    [testvg.test1 :refer [clock-component-test1 build-clock-state]]
    [testvg.test3 :refer [ build-startable-clock startable-clock-component]]))

(def bonus-state (atom [(build-startable-clock)]))



(defn bonus-page []
  [:div {:class "container"} "And now the bonus !"
   [:button {:class "btn btn-lg btn-primary"
             :on-click #(swap! bonus-state conj (build-startable-clock))} "Add clock"]
   [:div {:id "clocks"}
    (for [clock @bonus-state]
      ^{:key (:uuid @clock)} [:div {:class "bonus-clock"}
                              [startable-clock-component clock]
                              [:button {:class    "close"
                                        :on-click #(swap! bonus-state (fn [xs]
                                                                        (remove #{clock} xs)))} "X"]])]])