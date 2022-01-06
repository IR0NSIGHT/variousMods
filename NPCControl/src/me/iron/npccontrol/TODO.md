# Goal
- high convience, easy to install, one click does all
- pirate scout drone spawns randomly in loaded asteroid belts with active jammer, calls for pirate raid if not destroyed fast enough

Technicality:
- event system
    - starloader eventhandlers generate info code
    - fire all custom events that match code
    - binary code that contains event info
        - 0-7: faction type (part 0)
            0 neutral
            1 pirates
            2 npcs
            3 player faction
            4
            5
            6
            7
        - 8-15: entity type (part 1)
            0   ship
            1   station
            2   roid, roid managed
            3   astronaut
            4   sun
            5   blackhole
            6   shop
            7   planet core, ico, segment
        ... tbd
        - 49-56: cooldown in minutes 1..256 (4.2h) //disabled
        - 57-64: probability x/256 //disabled
    - timeout
    
- spawning
    - drone
    - pirate fleet
- automated blueprint download

- AI
    - custom fleet behaviour
        - drone: wait for contact, follow contact at distance
            - drone can only see what its scanners can see
        - pirates:
            - send message requesting ship to surrender (unfaction, unfleet, leave)
            - then capture ship
            - else attack and destroy
            - if enemy is to strong (outgunned), run away
    - custom ship behaviour
        - move close to unfactioned ship to capture
        - cease fire when ship surrenders
        - stop fighting and run away if fleet is told to do so
        
- config
    - ingame way to edit the ship BPs used for the pirate fleet



- PiratePrivateerProgram

// spawned with goal of capturing vessels 
//state (transition, next),..
- search target (found_target, check_target), (no_target_found, patrol_area) //search for a possible target in the neighbour sectors
```
     get objects in sectors
     //whats a proper target? ship with player? ai ship owned by player? > AI will attack anyways, cant properly surrender
     for each object:
       if is piloted by player
         program target = object
         (found_target, check_target)
         return
     (no_target_found, patrol_area)
 ```

- patrol_area (true, search_target) //move to random sector in area
```
//select random pos near center if one member at currentMovePos
//move to pos
//
    center = current_pos
    radius = 5
    dir = getRandomVec3
    offset = dir * radius
    currentMoveToPos = center + offset
```
- check target (target_factioned, warn_target),(target_unfactioned, capture_target), (target_is_AI, destroy_target)
```
    //test if how to handle target: can it be captured, warned
    if (target.faction == null) {    //just assumes that unfactioned ships cant be fleeted or AI.
        (target_unfactioned, capture_target);
    }
    
    if (target is AI / target is not piloted by player) { //AI cant surredner, warning is obsolete
        (target_is_AI, destroy target);
    }
    
    if (target is piloted by player) {
        (target_factioned, warn_target);
    }
```
- warn target (done, wait_for_surrender)
```
    send message to pilot: "you have 30 seconds to unfaction, unfleet and abandon ship. Otherwise you will be destroyed. Surrender and you will not be harmed.";
    (done, wait_for_surrender)
```
- wait for target to surrender (target_surrenders, capture_target), (target_attacks, destroy_target), (wait_time_up, destroy_target)
```
    //basically "check_target" but with a max timer
    waitTime --;
    if (waitTime <= 0)
        time is up
```
   destroy target (target_overheats, capture_target), (target_surrenders, capture_target)
   capture target (target_captured, go_home),(target_null, go_home), (target_factioned, check_target)
    go_home (all_ships_home, end) 
```
    //move order for all ship (formation cloud) to RTB to their FOB

```


# global piracy system
- pirate stations get classifications:
  - depot (unguarded warehouse)
  - blackmarket (idk yet)
  - forward operating base (raids start here, captured ships are brought back here)
- raids
  - raids start at FOB
  - target NPC or player ships
  - if a ship is caputred, its hauled back to the FOB and broken down/repaired there
    - captured ships without a classification are broken down for parts
    - ships with classification are repaired and used as pirate ships
  - the raid ends after the ship has been brought back to FOB
    - following pirates will make it possible to find the (hidden) FOB
  - FOBs should be near player or NPC systems in unclaimed systems, launching raids into NPC/player space there
  - FOBs should not occur to often, so that destroying one will have a meaningful impact
  - destroyed pirates drop hints about the location of the FOB or the FOBs name?
    - => a pirate raid should have identification so players can tell which FOB they come from
  - 

# pathing
find a safe path/corridor with 1 or more waypoints through the scene to the target position.
