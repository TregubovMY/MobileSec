import { generateAuthenticationOptions } from '@simplewebauthn/server';
import { handle, rpID, saveChallenge } from '../lib.js';

export default handle(async () => {
  const options = await generateAuthenticationOptions({ rpID, userVerification: 'preferred' });
  await saveChallenge(options.challenge, null);
  return { options: JSON.stringify(options) };
});
