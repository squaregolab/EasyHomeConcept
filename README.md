#Easy Home Concept

##AVERTISEMENT
Ce projet étant encore en developement, il ne peut en aucun cas étres utilisé en l'état dans une maison!

Nous n'avons pas encore abordé les differents problèmes liés à la securité ou encore l'interfacage avec (par exemple)
des volets de constructeur different.


##Principe du projet

Le but de ce projet est de proposer un système simple et à bas coup afin de pouvoir domotiser sa maison.

Le but final du projet étant de pouvoir contrôler les principaux acteurs de la maison (lumiere, volets, chauffage) ainsi que de pouvoir ajouter facilement au système divers acteurs.

##Solution utilisée

###Materiel

Pour le centre de controle, nous avons choisi une Raspberry Pi 3 ainssi qu'un écran tactile de 7".
Le choix s'est posé sur la Raspberry pi 3 car elle dispose du Wifi a l'origine ainsi que la facilité de connection d'un écran.

Pour ce qui est des modules de contrôle, nous avons fait le choix d'en disposer un dans chaque pièces (aspect modulaire). Ces modules sont équipés d'un Adafruit Huzzah ainsi qu'un GPIO Expander (MCP23008).


###Logiciel

Le programme de la Rapsberry est en fait le serveur, les modules sont donc les clients.

Le serveur ainsi que l'interface de la raspberry sont développé en Java (JavaFx pour l'interface) avec l'IDE Intellig IDEA.
Les différentes bibliotèques utilisées sont présente dans Libraries, les sources sont également disponibles.

Pour les Huzzahs, le programe est déveloper en C++/Arduino via PlatforIO (extention de Atom).
Les sources sont également disponible dans Sources.
