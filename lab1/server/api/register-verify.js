import { verifyRegistrationResponse } from '@simplewebauthn/server';
import { consumeChallenge, currentUser, db, handle, origin, rpID } from '../lib.js';

export default handle(async (req) => {
  const { uid } = await currentUser(req);

  const { verified, registrationInfo } = await verifyRegistrationResponse({
    response: JSON.parse(req.body.response),
    expectedChallenge: consumeChallenge(uid),
    expectedOrigin: origin,
    expectedRPID: rpID,
  });
  if (!verified) throw new Error('Passkey was not verified');

  const { credential } = registrationInfo;
  await db.collection('passkeys').doc(credential.id).set({
    uid,
    publicKey: Buffer.from(credential.publicKey).toString('base64url'),
    counter: credential.counter,
    transports: credential.transports ?? [],
  });
  return { ok: true };
});
