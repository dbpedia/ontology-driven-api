#!/usr/bin/env bash
set -e
source "${BASH_SOURCE%/*}/header.sh"

VERSION="1.2.1"

color_echo "Value Request to Version 1.0.0"
api_request "/values?entities=Barack_Obama&property=dbp:some-property-that-does-not-work-anymore&property=foaf:gender&oldVersion=true" 1.0.0
color_echo "Entities Request to Version 1.2.0"
api_request "/entities?value=Donald_Trump,old-dbo:nominee&value=Hillary_Clinton&filter=old-dbo:startDate,lt,2008-06-06" 1.2.0
color_echo "Request to Version 1.2.5 (Version not found)"
api_request "/values?entities=Barack_Obama&property=foaff:surname&property=foaf:gender" 1.2.5