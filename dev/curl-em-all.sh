#!/usr/bin/env bash

# This script makes requests for each request type

set -e
source "${BASH_SOURCE%/*}/curl-client/header.sh"

color_echo ">>> Curl 'Em all! <<<"
color_echo "LF 41 (Get Values of an entity/ entity list) - One entity"
api_request "/values?entities=Barack_Obama&property=foaf:surname&property=foaf:gender"

color_echo "LF 41 (Get Values of an entity/ entity list) - Entity List"
api_request "/values?entities=Barack_Obama,Donald_Trump&property=foaf:surname&property=foaf:gender"
color_echo "LF 42 (All Properties of Entity)"
api_request "/values?entities=St_Loup"
color_echo "LF 50 (Get all entities that are linked to either one of multiple specific values)"
api_request "/entities?setOp=intersection&value=Ethiopia&value=France"
color_echo "LF 51 (Get all entities that have one/multiple specific value(s) which are linked through specific properties to them)"
api_request "/entities?value=John_Oliver,dbp:guests&value=Warren_G._Harding,dbp:nominee"
color_echo "LF 60 (All instances of a class)"
api_request "/instances/SpaceStation?"
color_echo "LF 61 all instances with the properties"
api_request "/instances/SpaceStation?ofilter=dbp:launch&ofilter=dbp:crew"
api_request "/instances/Satellite?filter=dbp:launchSite&filter=dbp:landingSite&filter=dbp:missionType&filter=dbp:manufacturer&filter=dbp:recoveryBy"
color_echo "LF 62 (Filter instances of a class by property (optional or nonoptional))"
api_request "/instances/Person?ofilter=dbo:birthDate,eq,1941-09-27&filter=foaf:givenName,eq,Barack"
color_echo "LF 62 (Filter instances of a class by property-value-pair (interval))"
api_request "/instances/SpaceStation?ofilter=dbp:launch&ofilter=dbp:orbits,gt,50000"
api_request "/instances/SpaceStation?ofilter=dbp:launch&filter=dbp:orbits,gt,50000"
#color_echo "LF 63 (Only important)"
#api_request "/entities/Beverage?onlyImportant=true"
color_echo "LF 70 (Combination of all /entities-LFs)"
api_request "/instances/Person?value=Hank_Schrader,dbo:relative&filter=dbo:birthDate,gt,1965-2-11"
color_echo "LF 70 (Combination of all /entities-LFs)"
api_request "/entities?setOp=intersection&value=Kuiper_belt&filter=dbp:atmosphereComposition&ofilter=dbp:namedAfter"

api_request "/entities?setOp=intersection&value=Kuiper_belt&filter=dbo:meanTemperature,gt,25"

api_request "/entities?setOp=intersection&value=Trans-Neptunian_object,dbp:mpCategory&filter=dbp:mass,lt,2500&ofilter=dbo:discoverer"

