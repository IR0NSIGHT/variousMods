# Pathing
the pathing api is a program that finds a corridor/path through a scene.
the corridor is guaranteed to be free of any obstacles/at least x meters away from any obstacle.
The scene is an abstract representation of data, it holds objects with positions and radius, that are used as boundingspheres for collision.

the api is built as disconnected from starmade as possible.
to find a path, generate a scene, add objects to it and then instantiate a pathfinder.
give the scene to the pathfinder and tell the pf to find a path from a to b with given radius.
the returned path consists of positions that are the waypoints, connected by a straight line. the positions are
scene-positions, if you want to use them in starmade, you have to convert them to stellar positions.

## usage in starmade
since the pathfinder, raycast and abstractscene classes are very heavy abstractions, you can use the StarPathFinder to navigate
starmade sectors. its a child class of pathfinder intended to be used with starmade inputs/the starmade sector+position system.