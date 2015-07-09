(ns ^:figwheel-always testvg.test1
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [reagent.core :as reagent :refer [atom]]
    [cljs.core.async :refer [chan put! <! timeout]]
    [testvg.utils :as u]))


(defn build-clock-state
  ([]
   (build-clock-state (u/color-gen)))
  ([color]
   {:count  0
    :color color
    :name ""}))
;_________________________________________________
;                                                 |
;          View Components                        |
;_________________________________________________|


(defn clock-view [state]
  [:div {:style {:color (:color @state)}
         :class "time"}
   (u/fmt (js/Date. (:count @state)))])


;_________________________________________________
;                                                 |
;          State handling                         |
;_________________________________________________|

(defn clock-component-test1
  "A timer that starts automtocally when mounted and stops when unmounted"
  [state]
  (let [kill (chan)]
    (reagent/create-class
      {:reagent-render
       (fn [state]
         [clock-view state])
       :component-did-mount
       #(go-loop []
                 (let [[v _] (alts! [kill (timeout 1000)])]
                   (when-not (= v :kill)
                     (do
                       (swap! state update-in [:count] + 1000)
                       (recur)))))
       :component-will-unmount
       #(do
         (prn "Will unmont")
         (put! kill :kill))})))


(def test1-state (atom (build-clock-state)))

(defn test1-page []
  [:div
   [:h3 "Total time spent on this page :"]
   [clock-component-test1 test1-state]])
