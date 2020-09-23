(ns c20.core
  (:require [jsonista.core :as j]
            [cuerdas.core :as cu]
            [datascript.core :as d]
            [clojure.walk :as walk]
            [clojure.string :as cs]
            [clojure.java.io :as io])
  (:import (java.io File)))

(def src-categories
  ["Ability-Scores"
   "Classes"
   "Conditions"
   "Damage-Types"
   "Equipment-Categories"
   "Equipment"
   "Features"
   "Languages"
   "Levels"
   "Magic-Schools"
   "Monsters"
   "Proficiencies"
   "Races"
   "Skills"
   "Spellcasting"
   "Spells"
   "StartingEquipment"
   "Subclasses"
   "Subraces"
   "Traits"
   "Weapon-Properties"])

(defn src-link [src-category]
  (format "https://raw.githubusercontent.com/bagelbits/5e-database/master/src/5e-SRD-%s.json" (name src-category)))

(defn retrieve-files []
  (doseq [src-category src-categories
          :let [f (src-link src-category)
                l (io/file "resources" (last (cs/split f #"/")))]]
    (spit l (slurp f))))

(defn load-data [src]
  (-> src slurp (j/read-value j/keyword-keys-object-mapper)))

(comment
  (defonce web-data
           (zipmap
             (map cu/keyword src-categories)
             (map (comp load-data src-link) src-categories))))

(def data (->> "resources"
               io/file
               file-seq
               (filter (fn [^File file] (.isFile file)))
               (map load-data)))

(def schema
  {:url                          {:db/unique :db.unique/identity}
   :desc                         {:db/cardinality :db.cardinality/many}
   :components                   {:db/cardinality :db.cardinality/many}
   :ability-score                {:db/valueType :db.type/ref}
   :skills                       {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   :class                        {:db/valueType :db.type/ref}
   :subclass                     {:db/valueType :db.type/ref}
   :saving_throws                {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   :proficiencies                {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many
                                  :db/isComponent true}
   :cost                         {:db/valueType   :db.type/ref
                                  :db/isComponent true}
   :proficiency_choices          {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   :from                         {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   :subclasses                   {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   :subraces                     {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   :equipment                    {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   :damage_type                  {:db/valueType :db.type/ref}
   :school                       {:db/valueType :db.type/ref}
   :equipment_category           {:db/valueType :db.type/ref}
   :dc_type                      {:db/valueType :db.type/ref}
   :properties                   {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   :2h_damage                    {:db/valueType   :db.type/ref
                                  :db/isComponent true}
   ;:damage                       {:db/valueType   :db.type/ref
   ;                               :db/isComponent true}
   ;:range                        {:db/valueType   :db.type/ref
   ;                               :db/isComponent true}
   ; Range is sometimes a string. Should normalize this.
   :throw_range                  {:db/valueType   :db.type/ref
                                  :db/isComponent true}
   :typical_speakers             {:db/cardinality :db.cardinality/many}
   :features                     {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   :class_specific               {:db/valueType   :db.type/ref
                                  :db/isComponent true}
   :spellcasting                 {:db/valueType   :db.type/ref
                                  :db/isComponent true}
   :senses                       {:db/valueType   :db.type/ref
                                  :db/isComponent true}
   :speed                        {:db/valueType   :db.type/ref
                                  :db/isComponent true}
   :dc                           {:db/valueType   :db.type/ref
                                  :db/isComponent true}
   :area_of_effect               {:db/valueType   :db.type/ref
                                  :db/isComponent true}
   :feature_choices              {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   :language_options             {:db/valueType   :db.type/ref
                                  :db/isComponent true}
   :ability_bonus_options        {:db/valueType   :db.type/ref
                                  :db/isComponent true}
   :starting_proficiency_options {:db/valueType   :db.type/ref
                                  :db/isComponent true}
   :choice                       {:db/valueType   :db.type/ref
                                  :db/isComponent true}
   :references                   {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   :traits                       {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   :classes                      {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   ;Sometimes a csv for monster. :( Renamed to communication for monsters. Better way?
   :languages                    {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   ;Should special abilities be unique? See "/api/monsters/zombie" "Undead Fortitide" for an example.
   :special_abilities            {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many
                                  :db/isComponent true}
   :condition_immunities         {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   :starting_proficiencies       {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many}
   :starting_equipment           {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many
                                  :db/isComponent true}
   :starting_equipment_options   {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many
                                  :db/isComponent true}
   :equipment_option             {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many
                                  :db/isComponent true}
   :gear_category                {:db/valueType :db.type/ref}
   :damage                       {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many
                                  :db/isComponent true}
   :actions                      {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many
                                  :db/isComponent true}
   ;See Sphinx spellcasting ability. Has index, name, and url. "/api/ability-scores/5" Is it a ref?
   :ability                      {:db/valueType   :db.type/ref
                                  :db/isComponent true}
   ;Spells also specify a level for monsters. Should perhaps be {:level N :spell ref}
   ;The urls are also wrong. I bet they are out of date. For example
   ;   {:name "Spare the Dying",
   ;    :level 0,
   ;    :url "/api/spells/271"}
   ; URL should be "url": "/api/spells/spare-the-dying"
   :spells                       {:db/valueType   :db.type/ref
                                  :db/cardinality :db.cardinality/many
                                  :db/isComponent true}
   ;:damage_type                  {:db/valueType :db.type/ref}
   ;:ability_bonuses is problematic it needs to be a ref with a one-off on the ability. Should be similar to :starting_equipment
   ;School is also jacked up for monsters
   })

(defn denil [m]
  (walk/prewalk
    (fn [m]
      (if (map? m)
        (reduce (fn [m [k v]] (cond-> m (some? v) (assoc k v))) {} m)
        m))
    m))

;(->> (map denil data)
;     (reduce d/db-with (d/empty-db schema))
;     (take 100)
;     (filter (fn [[e a v t]] (map? v)))
;     (first))
;
;(nth (map denil data) 5)

(comment
  (->> "resources/5e-SRD-Ability-Scores.json" load-data))

(defn fix-monster-proficiencies []
  (letfn [(writer [data] (j/write-value-as-string data (j/object-mapper {:pretty true})))
          (load-data [src] (-> src slurp (j/read-value j/keyword-keys-object-mapper)))]
    (->> "resources/5e-SRD-Monsters.json"
         load-data
         (map (fn [{:keys [proficiencies] :as m}]
                (assoc m :proficiencies
                         (for [{:keys [value] :as proficiency} proficiencies]
                           {:value       value
                            :proficiency (dissoc proficiency :value)}))))
         writer
         (spit "resources/5e-SRD-Monsters-fixed.json"))))