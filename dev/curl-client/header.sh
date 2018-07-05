#!/usr/bin/env bash

# Source file for bash scripts that want to send requests to the API
# It parses command line options and provides two functions: color_echo and api_request

set -e
URI_START="http://localhost:8080/api"
URI_FORMAT="&format="
URI_PRETTY="&pretty=" 
VERSION="1.0.0"
KEY="8ecf58fe-5d48-4a8e-af37-8b7109f0d05a_ADMIN"
HELP_TEXT="Usage:\n -f [TSV|RDF|JSONLDF] for format\n -p [PREFIX|SHORT|NESTED] for prettyfication\n -s to walk you through every api-call (s stands for slow)\n -v [version] for setting the API version\n -k [key] sets the API Key\n"

# Colored output
function color_echo() {
  echo -e "\033[0;31m ${1} \033[0m"
}

# handle commandline options
while getopts ":f:p:v:k:hs" opt; do
    case $opt in
	f)
	    # -f format
	    if [ "$OPTARG" == "TSV" ] || [ "$OPTARG" == "RDF" ] || [ "$OPTARG" == "JSONLD" ]; then
		FORMAT=$OPTARG
	    else
		echo "Option -$OPTARG requires an argument: format can be [TSV|RDF|JSONLD] "  >&2
		exit 1
	    fi
	    ;;
	p)
	    #  -p prettification 
	    if [ "$OPTARG" == "PREFIXED" ] || [ "$OPTARG" == "NESTED" ]; then
		PRETTY=$OPTARG
	    else
		echo "Option -$OPTARG requires an argument: prettyfication can be [PREFIX|SHORT|NESTED] "  >&2
		exit 1
	    fi
	    ;;
	s)
	    # -s walks through every api call
	    SLOW=1
	   ;;
  k)  # -k sets the key
      KEY="${OPTARG}"
     ;;
  h) #help!
     color_echo ">>> Curl client for dbpedia rest api V1.0.0 <<<"
     printf "${HELP_TEXT}" >&2
	   exit 0
	   ;;
	v)
	    # -v sets the api request version
	    VERSION="${OPTARG}"
	   ;;
	\?) echo "Invalid Option -$OPTARG!"
      printf "${HELP_TEXT}" >&2
	    exit 1
	    ;;
	:) echo "Option -$OPTARG requires an argument!" >&2
     printf "${HELP_TEXT}" >&2
	   exit 1
	   ;;
    esac
done



# Sends a request to the API with curl
# The first argument is the path of the URI (after the version, e.g. "/values?entities")
# The second argument is optional and overrides the version number if set
# The format and pretty URI parameters are set if the "-f" or "-p" command line options are specified
function api_request() {
    if [ -n "$2" ]; then
      VERSION="$2"
    fi
    URI="${URI_START}/${VERSION}${1}"
    #URI="${URI}&key=8ecf58fe-5d48-4a8e-af37-8b7109f0d05a"
    URI="${URI}&key=${KEY}"
    if [ -n "$FORMAT" ]; then 
	    URI="$URI${URI_FORMAT}${FORMAT}"
    fi
    if [ -n "$PRETTY" ]; then
	    URI="$URI${URI_PRETTY}${PRETTY}"
    fi
    echo "Execute GET request to: ${URI}"
    if [ ${SLOW} ]; then
	    read -p "Press any key to continue... " -n1 -s
    fi
    curl -X GET "${URI}"
    echo "" #curl does not always end with a linebreak
}

