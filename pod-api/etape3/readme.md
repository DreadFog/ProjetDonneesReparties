# Etape 3 PdR

- Rajouter **transient** devant le champ obj dans SharedObject, pour ne pas emmener tout le graphe d'objets avec la sérialisation

- surcharger **readResolve** dans le SharedObject pour renvoyer le SharedObject local dans le couche client, et pas celui désérialisé, pour garder l'état cohérent