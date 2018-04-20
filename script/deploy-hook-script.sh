#!/bin/sh

set -e

for domain in $RENEWED_DOMAINS; do
        
		daemon_cert_root=/etc/nginx/certs
		
		# Make sure the certificate and private key files are
		# never world readable, even just for an instant while
		# we're copying them into daemon_cert_root.
		umask 077
		
		cp "$RENEWED_LINEAGE/fullchain.pem" "$daemon_cert_root/$domain.cert"
		cp "$RENEWED_LINEAGE/privkey.pem" "$daemon_cert_root/$domain.key"
		
		# Apply the proper file ownership and permissions for
		# the daemon to read its certificate and key.
		chown nginx "$daemon_cert_root/$domain.cert" \
		        "$daemon_cert_root/$domain.key"
		chmod 400 "$daemon_cert_root/$domain.cert" \
		        "$daemon_cert_root/$domain.key"
        
done

sudo systemctl reload nginx >/dev/null
