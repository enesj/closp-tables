(ns tables.ver01.svg-events
  (:use [com.rpl.specter :only [select transform setval FIRST LAST ALL keypath filterer srange comp-paths compiled-select collect-one compiled-setval]])
  (:require
    [goog.events :as events]
    [tables.ver01.table_data :as td]
    [tables.ver01.util :as u]
    [tables.ver01.analize :as an]))

(defn mouse-up []
  (fn [e]
    (.preventDefault e)
    (swap! td/tables-state assoc-in [:selection :active] false)
    (if  (or (not-empty (:selected (:selection @td/tables-state))) (not-empty (:ids @an/selected-current)))
      (swap! td/tables-state assoc-in [:selection :show] true)
      (swap! td/tables-state assoc-in [:selection :show] false))))


(defn table-events [full-state selection tables x y x-sel-s y-sel-s x-sel-e y-sel-e w h
                    tabale-selected selectected-path selection-active selection-offset selection-end selection-start selection-show]
  {:mouse-down (fn [e]
                 (.preventDefault e)
                 (let [x-current (+ (.-clientX e) (.-pageXOffset js/window) x)
                       y-current (+ (.-clientY e) (.-pageYOffset js/window) y)
                       start {:x x-current :y y-current}
                       end {:x1 x-current :y1 y-current}
                       direction (filterv boolean (doall (for [table
                                                               (conj (into (vals tables) (:borders @td/settings-base))
                                                                     {:id         :1 :x x-sel-s :y y-sel-s :width (- x-sel-e x-sel-s (- 0)) :height (- y-sel-e y-sel-s (- 0))
                                                                      :rect-right (+ x-sel-e 0) :rect-bottom (+ y-sel-e 0)})
                                                               :let [dir (u/collides-sel table {:id         1 :x x-current :y y-current :width 1 :height 1
                                                                                                :rect-right (+ x-current 1) :rect-bottom (+ y-current 1)} 0)]
                                                               :when (not= false dir)]
                                                           dir)))]
                   (if (empty? direction)
                     (do
                       (reset! an/selected-current {:current-state 0 :ids [] :tables {} :start {} :end {} :del false})
                       (swap! td/tables-state #(->> %
                                                    (compiled-setval selection-start start)
                                                    (compiled-setval selection-end end)
                                                    (compiled-setval selection-active true)
                                                    (compiled-setval selectected-path nil)
                                                    (compiled-setval tabale-selected false))))

                     (if (some #(= :1 %) direction)
                       (swap! td/tables-state #(->> %
                                                    (compiled-setval selection-offset {:x  (- x-current (:x (:start selection)))
                                                                                       :y  (- y-current (:y (:start selection)))
                                                                                       :x1 (- x-current (:x1 (:end selection)))
                                                                                       :y1 (- y-current (:y1 (:end selection)))})
                                                    (compiled-setval selection-active false)))
                       (do (reset! an/selected-current {:current-state 0 :ids [] :tables {} :start {} :end {} :del false})
                           (swap! td/tables-state
                                  #(compiled-setval tabale-selected false
                                                    (->> %
                                                         (compiled-setval selection-show false)
                                                         (compiled-setval selectected-path nil)
                                                         (compiled-setval selection-end nil)
                                                         (compiled-setval selection-start nil)))))))))
   :mouse-move (fn [e]
                   (.preventDefault e)
                   (let [x-current (+ (.-clientX e) (.-pageXOffset js/window) x)
                         y-current (+ (.-clientY e) (.-pageYOffset js/window) y)
                         start (:start selection)
                         end {:x1 x-current :y1 y-current}
                         offset (:offset selection)
                         [[x y] [x1 y1]] (u/start-end start end)]
                     (when (:active selection)
                       (let [sel (filterv boolean (doall (for [table (vals tables)]
                                                           (u/collides-sel-active table {:id         1 :x x :y y
                                                                                         :width      (- x1 x) :height (- y1 y)
                                                                                         :rect-right x1 :rect-bottom y1} 0))))
                             select-true (comp-paths :tables ALL LAST #(some (set sel) [(:id %)]) :selected)]
                         (swap! td/tables-state #(->> % (compiled-setval selection-show true)
                                                      (compiled-setval tabale-selected false)
                                                      (compiled-setval select-true true)
                                                      (compiled-setval selection-end end)
                                                      (compiled-setval selectected-path sel)))))

                     (when  (and (not (:show selection))  (seq (:selected selection)))
                       (if  (not= @an/selected-current {:current-state 0 :ids [] :tables {}})
                         (reset! an/selected-current {:current-state 0 :ids [] :tables {} :start {} :end {} :del false})))))
   :key-down   (fn [e]
                   (.preventDefault e)
                   (case (.-which e)
                     7 (swap! td/tables-state assoc-in [:snap] true)
                     nil)
                   (js/console.log (:snap @td/tables-state)))
   :key-up     (fn [e]
                   ;(.preventDefault e)
                   (case (.-which e)
                     7 (swap! td/tables-state assoc-in [:snap] false)
                     nil)
                   (js/console.log (:snap @td/tables-state)))})
