(ns ^:figwheel-always testvg.test3
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [reagent.core :as reagent :refer [atom]]
    [cljs.core.async :refer [chan put! <! timeout alts!]]
    [testvg.utils :as u]
    [testvg.test1 :refer [clock-component-test1 build-clock-state clock-view]]
    [clojure.string :as str]))


(def cmds {:start :stop
           :stop :start})


(defn flip-state
  [state]
  (swap! state update-in [:state] cmds))


(defn task-name [clock-state]
  [:div {:class "form-group"}
   [:h1
    [:input {:type        "text"
             :class       "form-control input-lg"
             :disabled (not (str/blank? (:name @clock-state)))
             ;:value       (:name @clock-state)
             :placeholder "Insert you task name"
             :on-blur     #(swap! clock-state assoc :name (u/e-value %))}]]])

(defn start-stop []
  (let [styles {:start "btn-primary"
                :stop "btn-danger"}]
    (fn [state]
      (let [{:keys [state comm]} @state
            status (cmds state)]
        [:button {:on-click #(put! comm :cmd)
                  :class    (str "btn btn-lg " (styles status))}
         (name status)]))))


(defn clock-component [state]
  [:div {:class "panel panel-default"}
   [:div {:class "panel-heading"
          :style {:background-color (:color @state)
                  :opacity 0.3}}
    [:div {:class "panel-title"}
     [task-name state]]]
   [:h1 "Time spent on task : "]
   [:div {:class "panel-body"}
    [clock-view state]]
   [:div {:class "panel-footer"}
    [:div {:class "btn-group"}
     [start-stop state]
     ]]])



(defn startable-clock-component [state]
  (with-meta clock-component
             {:component-will-unmount
              (prn "Component will unmount")
              :component-did-mount
              (let [{comm :comm} @state]
                (go-loop []
                         (<! comm)
                         (flip-state state)
                         (loop [c comm]
                           (let [[r _] (alts! [c (timeout 1000)])]
                             (if (= r :cmd)
                               (flip-state state)
                               (do
                                 (swap! state update-in [:count] + 1000)
                                 (recur c)))))
                         (recur)))}))



(defn build-startable-clock []
  (atom (assoc (build-clock-state (u/color-gen))
          :state :stop
          :comm (chan)
          :uuid (str (random-uuid)))))




(def state-page-3 (build-startable-clock))

(defn test3-page
  []
  [:div
   [:h2 "This is page 3"]
   [startable-clock-component state-page-3]
   [:div [:a {:href "#/"} "go to the home page"]]])
