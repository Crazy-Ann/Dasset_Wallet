package com.dasset.wallet.core.crypto.mnemonic;

import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.core.contant.Constant;
import com.dasset.wallet.core.contant.MnemonicDictionary;
import com.dasset.wallet.core.crypto.mnemonic.listener.OnMnemonicDictionaryResourcelistener;
import com.dasset.wallet.core.utils.Sha256Hash;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A MnemonicCode object may be used to convert between binary seed values and lists of words per
 * <a href="https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki">the BIP 39 specification</a>
 */


public class MnemonicCode {

    private MnemonicDictionary mnemonicDictionary = MnemonicDictionary.US;
    private ArrayList<String> dictionary;
    /**
     * UNIX time for when the BIP39 standard was finalised. This can be used as a default seed
     * birthday.
     */
    public static  long   BIP39_STANDARDISATION_TIME_SECS = 1381276800;
    private static String BIP39_ENGLISH_SHA256            = "ad90bf3beb7b0eb7e5acd74727dc0da96e0a280a258354e7293fb7e211ac03db";

    private static MnemonicCode                         mnemonicCode;
    private        OnMnemonicDictionaryResourcelistener onMnemonicDictionaryResourcelistener;

    private MnemonicCode() {
        // cannot be instantiated
    }

    public static synchronized MnemonicCode getInstance() {
        if (mnemonicCode == null) {
            mnemonicCode = new MnemonicCode();
        }
        return mnemonicCode;
    }

//    public static synchronized MnemonicCode getInstance(MnemonicDictionary mnemonicDictionary) {
//        if (mnemonicCode == null) {
//            mnemonicCode = new MnemonicCode();
//        }
//        if (mnemonicCode.getMnemonicDictionary(mnemonicDictionary) != null) {
//            mnemonicCode.dictionary = mnemonicCode.getMnemonicDictionary(mnemonicDictionary);
//            return mnemonicCode;
//        } else {
//            return null;
//        }
//    }

    public static void releaseInstance() {
        if (mnemonicCode != null) {
            mnemonicCode = null;
        }
    }

    public List<String> getDictionary() {
        return dictionary;
    }

    public void setDictionary(ArrayList<String> dictionary) {
        this.dictionary = dictionary;
    }

    public MnemonicDictionary getMnemonicDictionary() {
        return mnemonicDictionary;
    }

    public void setMnemonicDictionary(MnemonicDictionary mnemonicDictionary) {
        ArrayList<String> dictionary = getMnemonicDictionary(mnemonicDictionary);
        if (dictionary != null) {
            setDictionary(dictionary);
            this.mnemonicDictionary = mnemonicDictionary;
        }
    }

    public void setOnMnemonicDictionaryResourcelistener(OnMnemonicDictionaryResourcelistener onMnemonicDictionaryResourcelistener) {
        this.onMnemonicDictionaryResourcelistener = onMnemonicDictionaryResourcelistener;
    }


//    public static void setMnemonicCode(MnemonicCode mnemonicCode) {
//        MnemonicCode.mnemonicCode = mnemonicCode;
//    }

//    public static MnemonicCode instance() {
//        return mnemonicCode;
//    }

//    public MnemonicCode instanceForWord(MnemonicCode mnemonicCode, String word) {
//        if (mnemonicCode.getDictionary(word) == null) {
//            return null;
//        }
//        this.dictionary = mnemonicCode.getDictionary(word);
//        return mnemonicCode;
//    }

//    private ArrayList<String> getMnemonicDictionary(String word) {
//        try {
//            for (Object object : generateMnemonicDictionaries().entrySet()) {
//                Map.Entry         entry = (Map.Entry) object;
//                ArrayList<String> words = getWordListForInputStream((InputStream) entry.getValue());
//                if (words.contains(word)) {
//                    this.mnemonicDictionary = (MnemonicDictionary) entry.getKey();
//                    return words;
//                }
//            }
//        } catch (IOException | IllegalArgumentException e) {
//            e.printStackTrace();
//            return null;
//        }
//        return null;
//    }

    private ArrayList<String> getMnemonicDictionary(MnemonicDictionary mnemonicDictionary) {
        try {
            for (Object object : generateMnemonicDictionaries().entrySet()) {
                Map.Entry entry = (Map.Entry) object;
                if (entry.getKey().equals(mnemonicDictionary)) {
                    return getWordListForInputStream((InputStream) entry.getValue());
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            return null;
        }
        return null;
    }

    private HashMap<MnemonicDictionary, InputStream> generateMnemonicDictionaries() throws IOException, IllegalArgumentException {
        HashMap<MnemonicDictionary, InputStream> inputStreamMap = Maps.newHashMap();
        for (MnemonicDictionary mnemonicDictionary : MnemonicDictionary.getSupportedLanguages()) {
            if (onMnemonicDictionaryResourcelistener != null) {
                inputStreamMap.put(mnemonicDictionary, onMnemonicDictionaryResourcelistener.onMnemonicDictionaryResource(mnemonicDictionary));
            }
        }
        return inputStreamMap;
    }


    /**
     * Creates an MnemonicCode object, initializing with words read from the supplied input
     * stream.  If a wordListDigest
     * is supplied the digest of the words will be checked.
     */
//    public MnemonicCode() throws IOException, IllegalArgumentException {
//        for (Object object : generateMnemonicDictionaries().entrySet()) {
//            Map.Entry entry = (Map.Entry) object;
//            if (entry.getKey().equals(mnemonicDictionary)) {
//                InputStream inputStream = (InputStream) entry.getValue();
//                this.dictionary = getWordListForInputStream(inputStream);
//                return;
//            }
//        }
//    }
    public ArrayList<String> getWordListForInputStream(InputStream inputStream) throws IOException, IllegalArgumentException {
        BufferedReader    bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Constant.UTF_8));
        ArrayList<String> words          = Lists.newArrayListWithCapacity(2048);
        String            word;
        while ((word = bufferedReader.readLine()) != null) {
            words.add(word);
        }
        bufferedReader.close();
        if (words.size() != 2048) {
            throw new IllegalArgumentException("input stream did not contain 2048 words");
        }
        return words;
    }

    /**
     * Convert mnemonic word list to seed.
     */
    public byte[] toSeed(List<String> words, String passphrase) {
        // To create binary seed from mnemonic, we use PBKDF2 function
        // with mnemonic sentence (in UTF-8) used as a password and
        // string "mnemonic" + passphrase (again in UTF-8) used as a
        // salt. Iteration count is set to 4096 and HMAC-SHA512 is
        // used as a pseudo-random function. Desired length of the
        // derived key is 512 bits (= 64 bytes).
        //
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            stringBuilder.append(words.get(i));
            if (i < words.size() - 1) {
                stringBuilder.append(Regex.SPACE.getRegext());
            }
        }
        String pass  = stringBuilder.toString();
        String salt  = "mnemonic" + passphrase;
        long   start = System.currentTimeMillis();
        byte[] seed  = PBKDF2SHA512.derive(pass, salt, Constant.PBKDF2_ROUNDS, 64);
        LogUtil.getInstance().print(String.format("PBKDF2 took {%s}ms", System.currentTimeMillis() - start));
        return seed;
    }

    /**
     * Convert mnemonic word list to original entropy value.
     */
    public byte[] toEntropy(List<String> words) throws MnemonicException.MnemonicLengthException,
            MnemonicException.MnemonicWordException, MnemonicException.MnemonicChecksumException {
        if (words.size() % 3 > 0) {
            throw new MnemonicException.MnemonicLengthException("Word list size must be multiple of three words.");
        }
        if (words.size() == 0) {
            throw new MnemonicException.MnemonicLengthException("Word list is empty.");
        }

        // Look up all the words in the list and construct the
        // concatenation of the original entropy and the checksum.
        //
        int       concatLenBits = words.size() * 11;
        boolean[] concatBits    = new boolean[concatLenBits];
        int       wordindex     = 0;
        for (String word : words) {
            // Find the words index in the wordlist.
            int ndx = this.dictionary.indexOf(word);
            if (ndx < 0) {
                throw new MnemonicException.MnemonicWordException(word);
            }

            // Set the next 11 bits to the value of the index.
            for (int ii = 0; ii < 11; ++ii) {
                concatBits[(wordindex * 11) + ii] = (ndx & (1 << (10 - ii))) != 0;
            }
            ++wordindex;
        }

        int checksumLengthBits = concatLenBits / 33;
        int entropyLengthBits  = concatLenBits - checksumLengthBits;

        // Extract original entropy as bytes.
        byte[] entropy = new byte[entropyLengthBits / 8];
        for (int ii = 0; ii < entropy.length; ++ii) {
            for (int jj = 0; jj < 8; ++jj) {
                if (concatBits[(ii * 8) + jj]) {
                    entropy[ii] |= 1 << (7 - jj);
                }
            }
        }

        // Take the digest of the entropy.
        byte[]    hash     = Sha256Hash.create(entropy).getBytes();
        boolean[] hashBits = bytesToBits(hash);

        // Check all the checksum bits.
        for (int i = 0; i < checksumLengthBits; ++i) {
            if (concatBits[entropyLengthBits + i] != hashBits[i]) {
                throw new MnemonicException.MnemonicChecksumException();
            }
        }
        return entropy;
    }

    /**
     * Convert entropy data to mnemonic word list.
     */
    public List<String> toMnemonic(byte[] entropy) throws MnemonicException.MnemonicLengthException {
        if (entropy.length % 4 > 0) {
            throw new MnemonicException.MnemonicLengthException("Entropy length not multiple of 32 bits.");
        }
        if (entropy.length == 0) {
            throw new MnemonicException.MnemonicLengthException("Entropy is empty.");
        }

        // We take initial entropy of ENT bits and compute its
        // checksum by taking first ENT / 32 bits of its SHA256 hash.

        byte[]    hash     = Sha256Hash.create(entropy).getBytes();
        boolean[] hashBits = bytesToBits(hash);

        boolean[] entropyBits        = bytesToBits(entropy);
        int       checksumLengthBits = entropyBits.length / 32;

        // We append these bits to the end of the initial entropy. 
        boolean[] concatBits = new boolean[entropyBits.length + checksumLengthBits];
        System.arraycopy(entropyBits, 0, concatBits, 0, entropyBits.length);
        System.arraycopy(hashBits, 0, concatBits, entropyBits.length, checksumLengthBits);

        // Next we take these concatenated bits and split them into
        // groups of 11 bits. Each group encodes number from 0-2047
        // which is a position in a wordlist.  We convert numbers into
        // words and use joined words as mnemonic sentence.

        ArrayList<String> words = Lists.newArrayList();
        for (int i = 0; i < concatBits.length / 11; ++i) {
            int index = 0;
            for (int j = 0; j < 11; ++j) {
                index <<= 1;
                if (concatBits[(i * 11) + j]) {
                    index |= 0x1;
                }
            }
            words.add(this.dictionary.get(index));
        }
        return words;
    }

    /**
     * Check to see if a mnemonic word list is valid.
     */
    public void check(List<String> words) throws MnemonicException {
        toEntropy(words);
    }

    private static boolean[] bytesToBits(byte[] data) {
        boolean[] bits = new boolean[data.length * 8];
        for (int i = 0; i < data.length; ++i) {
            for (int j = 0; j < 8; ++j) {
                bits[(i * 8) + j] = (data[i] & (1 << (7 - j))) != 0;
            }
        }
        return bits;
    }
}
