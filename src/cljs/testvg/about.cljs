(ns ^:figwheel-always testvg.about)


(defn about-page []
  [:article {:class "container"}
   [:header
    [:h2 "About the testvg"]]
   [:section
    [:h3 "Om, Reagent and State"]
    "I am an Om person and it was a lot of fun to dive into Reagent !
    At first the two libraries seem very different but after a few hours I felt (quite) comfortable with Reagent.
    The biggest hurdle for me has been the transition form cursors to reagent/atom.
    Om is more directive and in Reagent you have more freedom. The problem I faced was to grok the best practices.
    I am used to have a single global state, but in the examples I saw a multiplication of different pieces of state.
    I don't like to def a piece of state and reference it in the body of a function several lines down.
    I had rather pass explicitly the state as an argument"]

   [:article
    [:h3 "The way I approached the problem"]
    "Starting from Test3 each solution is based on the previous test.
    I did not copy and paste code because I have been genetically modified to prevent it.
    I tried to compose the previous building blocks to come up with a solution for the next step.
    In Test3 I define a \"startable-component\" that can be controlled via a core.async channel.
    This generic component is used to define a \"startable-clock-component\". "]

   [:article
    [:h3 "Vigiglobe API"]
    [:section "The integration of the API was smooth, I was expecting to bypass CORS."]
    [:section
     "I have three charts that auto update every 5 seconds :"
     [:ul
      [:li "The first present the global volume for the vgteam-TV_Shows project "]
      [:li "The second discovers the different Shows, and displays the volume for each. "]
      [:li "The third presents the same data as the second but with a different representation. "]]
     ]
    [:section
     "I reused the startable-component defined in the Test-3 tp produce a generic \"startable-graph-component\" in order to start/pause the live data feed
      To produce a new graph the important part is to create a function that crunches the data returned by the API and mutates the Chart accordingly."]]])
