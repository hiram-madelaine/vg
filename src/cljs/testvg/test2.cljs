(ns ^:figwheel-always testvg.test2
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [reagent.core :as reagent :refer [atom]]
    [cljs.core.async :refer [chan put! <! timeout]]
    [testvg.utils :as u]
    [testvg.test1 :refer [clock-component-test1 build-clock-state]]
    [cljs.pprint :refer [pprint]]
    ))






(defn clock-component-test2 []
  (let [shared-state (atom (build-clock-state))]
    (fn []
      [:div
       [clock-component-test1 shared-state]
       [:pre [:code (with-out-str (pprint @shared-state))]]])))



(defn test2-page []
  [:div
   [:h3 "Show me your guts !"]
   [:h6 "(aka : shared state)"]
   [clock-component-test2]]
  )
