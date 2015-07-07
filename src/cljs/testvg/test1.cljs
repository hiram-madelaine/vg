(ns ^:figwheel-always testvg.test1
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [reagent.core :as reagent :refer [atom]]
    [cljs.core.async :refer [chan put! <! timeout]]
    [testvg.utils :as u]))



(defn build-clock-state
  ([]
   (build-clock-state "#ff3344"))
  ([color]
   {:count  0
    :color color
    :name ""}))

(def clock-state (atom (build-clock-state)))


;_________________________________________________
;                                                 |
;          View Components                        |
;_________________________________________________|

(defn color-input [clock-state]
  [:input {:type      "text"
           :class     "form-control input-lg"
           :value     (:color @clock-state)
           :on-change #(swap! clock-state assoc :color (u/e-value %))}])



(defn clock-view [state]
  [:div {:style {:color (:color @state)}
         :class "time"}
   (u/fmt (js/Date. (:count @state)))])


;_________________________________________________
;                                                 |
;          State handling                         |
;_________________________________________________|

(defn clock-component-test1 [state]
  (let [kill (chan)]
    (reagent/create-class
      {:reagent-render
       (fn [timer]
         [clock-view timer])
       :component-did-mount
       #(go-loop []
                 (let [[cmd c] (alts! [kill (timeout 1000)])]
                   (when-not (= c kill)
                     (do
                       (swap! state update-in [:count] + 1000)
                       (recur)))))
       :component-will-unmount
       #(put! kill :kill)})))



(defn test1-page []
  [:div
   [:h2 "Test page !"]
   [clock-component-test1 clock-state]
   [:div [:a {:href "#/"} "go to the home page"]]])
