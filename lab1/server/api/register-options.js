import { generateRegistrationOptions } from '@simplewebauthn/server';
import { currentUser, db, handle, rpID, saveChallenge } from '../lib.js';

export default handle(async (req) => {
  const { uid, email, name } = await currentUser(req);
  const existing = await db.collection('passkeys').where('uid', '==', uid).get();

  const options = await generateRegistrationOptions({
    rpName: 'Make it So',
    rpID,
    userName: email ?? uid,
    userDisplayName: name ?? email ?? uid,
    userID: new TextEncoder().encode(uid),
    excludeCredentials: existing.docs.map((doc) => ({
      id: doc.id,
      transports: doc.data().transports,
    })),
    authenticatorSelection: { residentKey: 'required', userVerification: 'preferred' },
  });
  delete options.hints;
  await saveChallenge(options.challenge, uid);
  return { options: JSON.stringify(options) };
});
