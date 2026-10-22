import { cert, getApps, initializeApp } from 'firebase-admin/app';
import { getAuth } from 'firebase-admin/auth';
import { getFirestore } from 'firebase-admin/firestore';

export const PACKAGE_NAME = 'com.google.firebase.example.makeitso';
export const CERT_SHA256 =
  '1C:BF:56:1C:34:50:CF:55:D5:2F:56:90:A9:32:7A:30:C8:E5:CF:CB:F7:70:FE:BC:3F:AE:67:1E:79:77:05:5D';

export const rpID = process.env.RP_ID ?? process.env.VERCEL_PROJECT_PRODUCTION_URL;
export const origin = `android:apk-key-hash:${Buffer.from(CERT_SHA256.replaceAll(':', ''), 'hex').toString('base64url')}`;

if (!getApps().length) {
  initializeApp({ credential: cert(JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT)) });
}

export const db = getFirestore();
export const auth = getAuth();

const CHALLENGE_TTL_MS = 5 * 60 * 1000;

export const saveChallenge = (challenge, uid) =>
  db.collection('challenges').doc(challenge).set({ uid, expiresAt: Date.now() + CHALLENGE_TTL_MS });

export const consumeChallenge = (uid) => async (challenge) => {
  const ref = db.collection('challenges').doc(challenge);
  const snap = await ref.get();
  if (!snap.exists) return false;
  await ref.delete();
  const data = snap.data();
  return data.expiresAt > Date.now() && data.uid === uid;
};

export const currentUser = async (req) => {
  const token = req.headers.authorization?.replace('Bearer ', '');
  return auth.verifyIdToken(token);
};

export const handle = (fn) => async (req, res) => {
  if (req.method !== 'POST') return res.status(405).json({ error: 'POST only' });
  try {
    res.status(200).json(await fn(req));
  } catch (e) {
    res.status(400).json({ error: e.message });
  }
};
