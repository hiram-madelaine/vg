(ns testvg.navigation)




(defn nav-bar []
  [:ul {:class "nav nav-tabs"}
   [:li [:a {:href "#/"} "Home"]]
   [:li [:a {:href "#/about"} "About"]]
   [:li [:a {:href "#/test1"} "Test 1"]]
   [:li [:a {:href "#/test2"} "Test 2"]]
   [:li [:a {:href "#/test3"} "Test 3"]]
   [:li [:a {:href "#/bonus"} "Bonus"]]
   [:li [:a {:href "#/chart"} "Chart"]]])
