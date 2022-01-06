# Plan
Build a capable AI that can behave similar to a soldier in Arma 3:
- shoot order //feuerstatus grün
- move order //move 500m front
- apply behaviour such as stealth, ignore_enemies, hold_fire etc //hold fire!, stealth!
- be assigned a specific target //soldier attack that rifleman

# Technicalities
- movement and firing need to be separete machines. They are not mutually exclusive states.
A ship needs to be able to move AND fire at the same time
- movement does not differentiate between sector-to-sector movement or in-sector-movement.

# Steps
- bewegungs FMS: ship fliegt immer to move_to_pos asap, pos wird von aussen verändert
    - bewegungs machine intern handelt Dinge wie:
        - rotiere
        - fliege vorwärts rückwärts
        - kollisions vermeidung

AiShip
fields:
health_state (green, yellow, red, black)
current_order (move, attack,)

//allowed addon usage -> controlled by group/fleet
use_jump
use_stealth
use_inhibitor

//private
move_to_pos(Vec3 sector, Vec3 pos, rotation)
current_target(Object)

//or maybe separete shield and healthpoints?
FSM


potential orders:
ship attack this object
ship hold fire
ship move there, ignore all enemies

