# remote server deployment

- appserver: provides position api and positioning service

- webui: basic map rendering, use with [zkjs/tools/bledevices](https://github.com/zkjs/tools/)

## AP naming scheme:

`ENTITY_ALIAS.SERIAL`, in which `ENTITY_ALIAS`: 4-letter/digit combination of `2,3,4...9` and `A,B,C...H,J,...,N,P,Q,...,Z` (34^4 = 1336336)
e.x. entity a with alias `AL3XK` and ap serial `16` => AP.name: `AL3XK.16`

