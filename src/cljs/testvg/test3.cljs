(ns ^:figwheel-always testvg.test3
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [reagent.core :as reagent :refer [atom]]
    [cljs.core.async :refer [chan put! <! timeout alts!]]
    [testvg.test1 :refer [clock-component-test1 build-clock-state clock-component]]))



(def cmds {:start :stop
           :stop :start})


(defn flip-state
  [state]
  (swap! state update-in [:state] cmds))

(defn clock-component-test3 [timer cmd]
  (with-meta clock-component
             {:component-will-unmount
              (prn "Component will unmount")
              :component-did-mount
              (go-loop []
                       (<! cmd)
                       (flip-state timer)
                       (loop [c cmd]
                         (let [[r _] (alts! [c (timeout 1000)])]
                           (if (= r :cmd)
                             (flip-state timer)
                             (do
                               (swap! timer assoc :date (js/Date.))
                               (recur c)))))
                       (recur))}))

(defn start-stop []
  (let [styles {:start "btn-primary"
                :stop "btn-danger"}]
    (fn [timer cmd]
     (let [status (-> @timer :state cmds)]
       [:div
        [:button {:on-click #(put! cmd :cmd)
                  :class    (str "btn btn-lg " (styles status))} (name status)]]))))



(defn startable-clock-component
  []
  (let [comm (chan)
        state (atom (assoc (build-clock-state "#008844") :state :stop))]
    (fn []
      [:div
       [clock-component-test3 state comm]
       [start-stop state comm]
       ])))

(defn test3-page []
  [:div
   [:h2 "This is page 3"]
   [startable-clock-component]
   [:div [:a {:href "#/"} "go to the home page"]]])
