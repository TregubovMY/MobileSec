import { CERT_SHA256, PACKAGE_NAME } from '../lib.js';

export default function handler(req, res) {
  res.status(200).json([
    {
      relation: [
        'delegate_permission/common.get_login_creds',
        'delegate_permission/common.handle_all_urls',
      ],
      target: {
        namespace: 'android_app',
        package_name: PACKAGE_NAME,
        sha256_cert_fingerprints: [CERT_SHA256],
      },
    },
  ]);
}
