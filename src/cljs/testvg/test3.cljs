(ns ^:figwheel-always testvg.test3
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [reagent.core :as reagent :refer [atom]]
    [cljs.core.async :refer [chan put! <! timeout alts! close!]]
    [testvg.utils :as u]
    [testvg.test1 :refer [clock-component-test1 build-clock-state clock-view]]
    [clojure.string :as str]))


(def cmds {:start :stop
           :stop :start})


(defn flip-state!
  [state]
  (swap! state update-in [:state] cmds))


(defn task-name
  "Give a name to the task."
  [clock-state]
  [:div {:class "form-group"}
   [:h1
    [:input {:type        "text"
             :class       "form-control input-lg"
             :disabled (not (str/blank? (:name @clock-state)))
             ;:value       (:name @clock-state)
             :placeholder "Insert you task name"
             :on-blur     #(swap! clock-state assoc :name (u/e-value %))}]]])

(defn start-stop
  "Display a button that start/stop the counbter. "
  []
  (let [styles {:start "btn-primary"
                :stop "btn-danger"}]
    (fn [state]
      (let [{:keys [state comm]} @state
            status (cmds state)]
        [:button {:on-click #(put! comm status)
                  :class    (str "btn btn-lg " (styles status))}
         (name status)]))))


(defn clock-component
  "Diplay the full task"
  [state]
  [:div {:class "clock"}
   [:div {:class "panel panel-default"}
    [:div {:class "panel-heading"
           :style {:background-color (:color @state)
                   :opacity          0.3}}
     [:div {:class "panel-title"}
      [task-name state]]]
    [:div {:class "panel-body"}
     [:h1 "Time spent on task : "]
     [clock-view state]]
    [:div {:class "panel-footer"}
     [:div {:class "btn-group"}
      [start-stop state]]]]])



(defn startable-clock-component
  "Add the dynamic with core.async"
  [state]
  (reagent/create-class
    {:reagent-render
     (fn [state]
       [clock-component state])
     :component-did-mount
     #(let [{:keys [comm go-chan]} @state]
       (when-not go-chan
         (let [go-chan (go-loop []
                        (let [cmd (<! comm)]
                          (do
                            (flip-state! state)
                            (loop [c comm]
                              (let [[r cmd] (alts! [c (timeout 1000)])]
                                (condp = cmd
                                  comm (flip-state! state)
                                  (do
                                    (swap! state update-in [:count] + 1000)
                                    (recur c)))))
                            (recur))))]
           (swap! state assoc :go-chan go-chan))))}))



(defn build-startable-clock []
  (atom (assoc (build-clock-state (u/color-gen))
          :state :stop
          :comm (chan)
          :uuid (str (random-uuid)))))


(defn test3-page
  []
  [:div
   [:h2 "This is page 3"]
   [startable-clock-component (build-startable-clock)]
   [:div [:a {:href "#/"} "go to the home page"]]])
