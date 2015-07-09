(ns ^:figwheel-always testvg.about)


(defn about-page []
  [:div {:class "container"}
   [:h2 "About the testvg"]

   [:h3 "Om, Reagent and State"]
   [:section "I am an Om person and it was a lot of fun to dive into Reagent !
    At first the two libraries seem very different but after a few hours I felt (quite) comfortable with Reagent.
    The biggest hurdle for me has been the transiton form cursors to reagent/atom.
    Om is more directive and in Reagent you have more freedom. The problem I faced was to grok the best practices.
    I am used to have a single global state, but in the examples I saw a multiplication of different pieces of state.
    I don't like to def a piece of state and reference it in the body of a function several lines down.
    I had rather pass explicitely the state as an argument"]
   [:h3 "The way I approched the problem"]
   [:section
    "Starting from Test3 each solution is based on the previous test.
    I did not copy and paste code because I have been genetically modified to prevent it.
    I tried to compose the previous building blocks to come up with a solution.
    In Test3 I define a \"startable-component\" that can be controled via a core.async channel.
    This generic component is used to define a \"startable-clock-compoent\".
    In the \"Vigiglobe API\" exercice the same generic component is used to start and stop the live data update. "]
   [:h3 "Vigiglobe API"]
   [:section
    "The integration of the API was smooth, I was expecting to bypass CORS ;-)
    I have two chart that auto update every 5 seconds.
    The first present the global volume for the vgteam-TV_Shows project
    The second discovers the different Shows, and displays the volume for each.
    "]])
