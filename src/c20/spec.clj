(ns c20.spec
  (:require [clojure.spec.alpha :as s]
            [c20.core :as core]))

;;https://www.dnd5eapi.co/docs/#common-models-section

(s/def ::index string?)
(s/def ::name string?)
(s/def ::class string?)
(s/def ::full_name string?)
(s/def ::url (s/and string? #(re-matches #"(?:/[^/]+)*" %)))

(s/def ::desc (s/coll-of string?))

(s/def ::named-resource (s/keys :req-un [::name ::class ::url]))
(s/def ::api-reference (s/keys :req-un [::index ::name ::url]))
(s/def ::api-references (s/coll-of ::api-reference))
(s/def ::class-api-resource (s/keys :req-un [::index ::class ::url]))

(s/def ::api-ref-with-desc (s/and ::api-reference (s/keys :req-un [::desc])))
(s/def ::api-ref-with-descs (s/coll-of ::api-ref-with-desc))

(s/def ::choose pos-int?)
(s/def ::type string?)
(s/def ::from ::api-references)
(s/def ::choice (s/keys :req-un [::choose ::type ::from]))
(s/def ::cost (s/keys :req-un [::quantity ::unit]))

(s/def ::skills ::api-references)

(s/def ::srd-ability-score (s/and
                             ::api-reference
                             (s/keys :req-un [::desc ::skills ::full_name])))
(s/def ::srd-ability-scores (s/coll-of ::srd-ability-score))

(s/def ::class_levels ::url)
(s/def ::hit_die pos-int?)
(s/def ::proficiencies ::api-references)
(s/def ::proficiency_choices (s/coll-of ::choice))
(s/def ::saving_throws ::api-references)
(s/def ::spellcasting ::url)
(s/def ::spells ::url)
(s/def ::starting_equipment ::url)
(s/def ::subclasses ::api-references)

(s/def ::srd-non-spell-casting-class (s/and
                                       ::api-reference
                                       (s/keys :req-un [::class_levels
                                                        ::hit_die
                                                        ::proficiencies
                                                        ::proficiency_choices
                                                        ::saving_throws
                                                        ::starting_equipment
                                                        ::subclasses])))

(s/def ::srd-spell-casting-class (s/and
                                   ::srd-non-spell-casting-class
                                   (s/keys :req-un [::spells ::spellcasting])))

(s/def ::srd-class (s/or
                     :srd-non-spell-casting-class ::srd-non-spell-casting-class
                     :srd-spell-casting-class ::srd-spell-casting-class))

(s/def ::srd-classes (s/coll-of ::srd-class))

(s/def ::srd-conditions ::api-ref-with-descs)
(s/def ::srd-damage-types ::api-ref-with-descs)

(s/def ::srd-basic-equipment
  (s/and
    ::api-reference
    (s/keys :req-un [::cost ::equipment_category]
            :opt-un [::weight ::desc])))

(s/def ::srd-tools
  (s/and
    ::srd-basic-equipment
    (s/keys :req-un [::tool_category ::weight]
            :opt-un [])))

(s/def ::srd-gear
  (s/and
    ::srd-basic-equipment
    (s/keys :req-un [::gear_category]
            :opt-un [::contents ::weight])))

(s/def ::srd-mounts-and-vehicles
  (s/and
    ::srd-basic-equipment
    (s/keys :req-un [::vehicle_category]
            :opt-un [::speed])))

(s/def ::srd-any-equipment
  (s/or :tools ::srd-tools
        :gear ::srd-gear
        :basic-equipment
        (s/and
          ::srd-basic-equipment
          (s/keys :req-un [::2h_damage
                           ::armor_category
                           ::armor_class
                           ::capacity
                           ::category_range
                           ::contents
                           ::cost
                           ::damage
                           ::desc
                           ::equipment_category
                           ::gear_category
                           ::properties
                           ::quantity
                           ::range
                           ::special
                           ::speed
                           ::stealth_disadvantage
                           ::str_minimum
                           ::throw_range
                           ::tool_category
                           ::vehicle_category
                           ::weapon_category
                           ::weapon_range
                           ::weight]))
        :mount-or-vehicle ::srd-mounts-and-vehicles))
(s/def ::srd-equipment (s/coll-of ::srd-any-equipment))


(->> "resources/5e-SRD-Equipment.json" core/load-data last keys sort vec)
;(->> "resources/5e-SRD-Classes.json" core/load-data (take 4))
;(->> "resources/5e-SRD-Classes.json" core/load-data (s/explain ::srd-classes))

(comment
  (s/valid?
    ::api-reference
    {:desc      ["Strength measures bodily power, athletic training, and the extent to which you can exert raw physical force."
                 "A Strength check can model any attempt to lift, push, pull, or break something, to force your body through a space, or to otherwise apply brute force to a situation. The Athletics skill reflects aptitude in certain kinds of Strength checks."],
     :index     "str",
     :name      "STR",
     :skills    [{:index "athletics", :name "Athletics", :url "/api/skills/athletics"}],
     :url       "/api/ability-scores/str",
     :full_name "Strength"})

  (->> "resources/5e-SRD-Ability-Scores.json" core/load-data (s/valid? ::srd-ability-scores))
  (->> "resources/5e-SRD-Classes.json" core/load-data (s/valid? ::srd-classes))
  (->> "resources/5e-SRD-Conditions.json" core/load-data (s/valid? ::srd-conditions))
  (->> "resources/5e-SRD-Damage-Types.json" core/load-data (s/valid? ::srd-damage-types))
  (->> "resources/5e-SRD-Equipment.json" core/load-data (s/explain ::srd-equipment))
  )
