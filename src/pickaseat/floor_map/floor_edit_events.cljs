(ns pickaseat.floor-map.floor-edit-events
  (:require
    [pickaseat.data.floor-data :as floor-data]))

(defn mouse-up []
  (fn [e]
    (.preventDefault e)
    ;(js/console.log "up")
    (swap! floor-data/floor-state assoc-in [:selection :active] false)
    (if  (not-empty (:selected (:selection @floor-data/floor-states-data)))
      (swap! floor-data/floor-state assoc-in [:selection :show] true)
      (swap! floor-data/floor-state assoc-in [:selection :show] false))))



