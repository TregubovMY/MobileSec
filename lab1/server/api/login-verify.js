import { verifyAuthenticationResponse } from '@simplewebauthn/server';
import { auth, consumeChallenge, db, handle, origin, rpID } from '../lib.js';

export default handle(async (req) => {
  const response = JSON.parse(req.body.response);
  const snap = await db.collection('passkeys').doc(response.id).get();
  if (!snap.exists) throw new Error('Unknown passkey');
  const passkey = snap.data();

  const { verified, authenticationInfo } = await verifyAuthenticationResponse({
    response,
    expectedChallenge: consumeChallenge(null),
    expectedOrigin: origin,
    expectedRPID: rpID,
    credential: {
      id: snap.id,
      publicKey: new Uint8Array(Buffer.from(passkey.publicKey, 'base64url')),
      counter: passkey.counter,
      transports: passkey.transports,
    },
  });
  if (!verified) throw new Error('Passkey was not verified');

  await snap.ref.update({ counter: authenticationInfo.newCounter });
  return { token: await auth.createCustomToken(passkey.uid) };
});
