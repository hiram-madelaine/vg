(ns ^:figwheel-always testvg.bonus
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [reagent.core :as reagent :refer [atom]]
    [cljs.core.async :refer [chan put! <! timeout alts!]]
    [testvg.test1 :refer [clock-component-test1 build-clock-state]]
    [testvg.test3 :refer [ build-startable-clock startable-clock-component]]))

(def bonus-state (atom [(build-startable-clock)]))



(defn bonus-page []
  [:div {:class ""}
   [:h3 "Track time spent on your all your tasks"]
   [:div {:class "navbar navbar-default"}
    [:div {:class "container-fluid"}
     [:button {:class    "btn btn-default navbar-btn"
               :on-click #(swap! bonus-state conj (build-startable-clock))} "Add a task"]]]
   [:div {:id "clocks"}
    (doall (for [clock @bonus-state]
             ^{:key (:uuid @clock)} [:div {:class "bonus-clock"}
                                     [startable-clock-component clock]
                                     [:button {:class    "close"
                                               :on-click #(swap! bonus-state (fn [xs]
                                                                               (remove #{clock} xs)))} "X"]]))]])