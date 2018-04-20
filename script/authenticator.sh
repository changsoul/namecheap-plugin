#!/bin/bash

APP_NAME="/var/local/script/plugin/namecheap-plugin-1.0.0.jar"
API_KEY="hnkc"
EMAIL="3cuAev"

# Strip only the top domain to get the zone id
DOMAIN=$(expr match "$CERTBOT_DOMAIN" '.*\.\(.*\..*\)')

echo "certbot domain: $CERTBOT_DOMAIN"
echo "certbot validation: $CERTBOT_VALIDATION"
echo "substring domain: $DOMAIN"

sudo java -jar $APP_NAME --add --user=$EMAIL --pwd=$API_KEY --domain=$CERTBOT_DOMAIN --key=_acme-challenge --value=$CERTBOT_VALIDATION

# Sleep to make sure the change has time to propagate over to DNS
sleep 25
