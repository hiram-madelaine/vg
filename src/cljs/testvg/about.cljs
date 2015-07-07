(ns ^:figwheel-always testvg.about)


(defn about-page []
  [:div
   [:h2 "About the testvg"]

   [:h3 "Om, Reagent and State"]
   [:pre "I am an Om person and it was fun to dive into Reagent.
At first the two libraries seem very different but after a few hours I felt comfortable with Reagent.
The biggest hurdle for me has been the transiton form cursors to reagent/atom.
In the examples I saw a multiplication of different pieces of state in atoms.
I am used to have a global state that is passed from component
I don't like to def a piece of state and reference it in the body of a function several lines down.
I had rather pass explicitely the state as an argument "]
   ])
