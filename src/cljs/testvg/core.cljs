(ns ^:figwheel-always testvg.core
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [testvg.test1 :refer [test1-page]]
              [testvg.test3 :refer [test3-page]])
    (:import goog.History))



;; -------------------------
;; Views

(defn home-page []
  [:div [:h2 "Welcome to testvg"]
   [:div [:a {:href "#/about"} "go to about page"]]
   [:div [:a {:href "#/test1"} "go to about test1"]]
   [:div [:a {:href "#/test3"} "go to about test3"]]])

(defn about-page []
  [:div [:h2 "About testvg"]
   [:div [:a {:href "#/"} "go to the home page"]]])


(defn current-page []
  [:div [(session/get :current-page)]])



;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/test1"
                    []
                    (session/put! :current-page #'test1-page))

(secretary/defroute "/test3"
                    []
                    (session/put! :current-page #'test3-page))


;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
