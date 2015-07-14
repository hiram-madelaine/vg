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
  "stop <-> start"
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
  "Display a button that can start/stop the counter."
  ([state styles]
   (let [{:keys [state comm]} @state
         status (cmds state)]
     [:button {:on-click #(put! comm status)
               :class    (str "btn btn-lg " (styles status))}
      (name status)]))
  ([state]
    (start-stop state {:start "btn-primary"
                       :stop "btn-danger"})))


(defn clock-component
  "Diplay the full Timer Task component"
  [state]
  [:div {:class "clock panel panel-default"}
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
     [start-stop state]]]])


(defn startable-component
  "HOF to create a startable component
  comp : reagent component used for the rendering
  action : callback called every timeout. It is passed 2 args : the state and the time-out"
  [comp action time-out]
  (fn [state]
    (reagent/create-class
      {:reagent-render
       (fn [state]
         [comp state])
       :component-did-mount
       #(let [{:keys [comm go-chan]} @state]
         (when-not go-chan
           (let [go-chan (go-loop []
                                  (<! comm)
                                  (flip-state! state)
                                  (loop [c comm]
                                    (let [[r cmd] (alts! [c (timeout time-out)])]
                                      (condp = cmd
                                        comm (flip-state! state)
                                        (do
                                          (action state time-out)
                                          (recur c)))))
                                  (recur))]
             (swap! state assoc :go-chan go-chan))))})))


(def startable-clock-component
  (startable-component clock-component #(swap! % update-in [:count] + %2) 1000))

(defn build-startable-clock
  []
  (atom (assoc (build-clock-state (u/color-gen))
          :state :stop
          :comm (chan)
          :uuid (str (random-uuid)))))


(def test3-state (build-startable-clock))

(defn test3-page
  []
  [:div
   [:h3 "This Timer can be started and stopped at will."]
   [startable-clock-component test3-state]])
