package app.ezanpause;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Vakit namazlarının rekât, hüküm ve okunacak dua/sûre bilgileri (Hanefî mezhebi, Diyanet anlatımı).
 * Ayrıntılı ve kişisel durumlar için ilmihale veya müftülüğe başvurulmalıdır.
 */
final class PrayerGuide {
    private PrayerGuide() {}

    static final class Part {
        final String name;      // örn. "Sabahın sünneti"
        final int rekat;
        final String ruling;    // örn. "Müekkede sünnet"
        final String[] steps;   // her rekât için bir satır
        final String[] duaIds;  // bu bölümde okunan dualar

        Part(String name, int rekat, String ruling, String[] steps, String[] duaIds) {
            this.name = name;
            this.rekat = rekat;
            this.ruling = ruling;
            this.steps = steps;
            this.duaIds = duaIds;
        }
    }

    static final class Prayer {
        final String key;
        final String title;
        final String timeInfo;
        final String note;
        final List<Part> parts;

        Prayer(String key, String title, String timeInfo, String note, List<Part> parts) {
            this.key = key;
            this.title = title;
            this.timeInfo = timeInfo;
            this.note = note;
            this.parts = parts;
        }

        int totalRekat() {
            int t = 0;
            for (Part p : parts) t += p.rekat;
            return t;
        }
    }

    static final class Dua {
        final String id;
        final String name;
        final String when;
        final String arabic;
        final String translit;
        final String meaning;

        Dua(String id, String name, String when, String arabic, String translit, String meaning) {
            this.id = id;
            this.name = name;
            this.when = when;
            this.arabic = arabic;
            this.translit = translit;
            this.meaning = meaning;
        }
    }

    // ------------------------------------------------------------------ rekât adımları (ortak şablonlar)

    /** 2 rekât (sabah sünneti/farzı, öğle/akşam/yatsı son sünneti). */
    private static String[] iki() {
        return new String[]{
                "1. rekât: Niyet edip \"Allâhü ekber\" diyerek tekbir alınır, eller bağlanır. Sübhâneke okunur, "
                        + "Euzü-Besmele çekilir, Fâtiha okunur, ardından zamm-ı sûre (örn. Kevser, İhlâs, Fîl) okunur. "
                        + "Rükû, kavme, iki secde yapılır.",
                "2. rekât: Besmele ile Fâtiha, zamm-ı sûre okunur (Sübhâneke okunmaz). Rükû ve iki secdeden sonra son oturuşta "
                        + "Ettehiyyâtü, Allâhümme salli, Allâhümme bârik, Rabbenâ duaları okunur. Önce sağa, sonra sola "
                        + "\"Esselâmü aleyküm ve rahmetullâh\" diyerek selam verilir."
        };
    }

    /** 4 rekât müekkede sünnet (öğlenin ilk sünneti, cuma sünnetleri). */
    private static String[] dortSunnetMuekkede() {
        return new String[]{
                "1. rekât: Sübhâneke, Euzü-Besmele, Fâtiha, zamm-ı sûre; rükû, iki secde.",
                "2. rekât: Besmele, Fâtiha, zamm-ı sûre; rükû, iki secde. İlk oturuşta yalnız Ettehiyyâtü okunur, kalkılır.",
                "3. rekât: Besmele, Fâtiha, zamm-ı sûre (sünnette 3. ve 4. rekâtta da sûre okunur); rükû, iki secde.",
                "4. rekât: Besmele, Fâtiha, zamm-ı sûre; rükû, iki secde. Son oturuşta Ettehiyyâtü, Allâhümme salli, "
                        + "Allâhümme bârik, Rabbenâ okunur ve selam verilir."
        };
    }

    /** 4 rekât gayri müekkede sünnet (ikindi ve yatsının ilk sünneti). */
    private static String[] dortSunnetGayri() {
        return new String[]{
                "1. rekât: Sübhâneke, Euzü-Besmele, Fâtiha, zamm-ı sûre; rükû, iki secde.",
                "2. rekât: Besmele, Fâtiha, zamm-ı sûre; rükû, iki secde. İlk oturuşta Ettehiyyâtü, Allâhümme salli ve "
                        + "Allâhümme bârik okunur, kalkılır.",
                "3. rekât: Bu rekâtta tekrar Sübhâneke okunur (gayri müekkede sünnete özgü), Euzü-Besmele, Fâtiha ve zamm-ı sûre; "
                        + "rükû, iki secde.",
                "4. rekât: Besmele, Fâtiha, zamm-ı sûre; rükû, iki secde. Son oturuşta Ettehiyyâtü, Allâhümme salli, "
                        + "Allâhümme bârik, Rabbenâ okunur ve selam verilir."
        };
    }

    /** 4 rekât farz (öğle, ikindi, yatsı). */
    private static String[] dortFarz() {
        return new String[]{
                "Namaza kalkmadan önce kamet getirilir. Farz niyetiyle tekbir alınır.",
                "1. rekât: Sübhâneke, Euzü-Besmele, Fâtiha, zamm-ı sûre; rükû, iki secde.",
                "2. rekât: Besmele, Fâtiha, zamm-ı sûre; rükû, iki secde. İlk oturuşta yalnız Ettehiyyâtü okunur, kalkılır.",
                "3. rekât: Yalnız Fâtiha okunur, sûre OKUNMAZ (farzın 3. ve 4. rekâtında zamm-ı sûre yoktur); rükû, iki secde.",
                "4. rekât: Yalnız Fâtiha; rükû, iki secde. Son oturuşta Ettehiyyâtü, Allâhümme salli, Allâhümme bârik, "
                        + "Rabbenâ okunur ve selam verilir. Ardından tesbihat yapılır."
        };
    }

    // ------------------------------------------------------------------ vakitler

    static final List<Prayer> PRAYERS = new ArrayList<>();
    static final List<Dua> DUAS = new ArrayList<>();

    static {
        final String[] iki = iki();

        PRAYERS.add(new Prayer("imsak", "İmsak / Sabah namazı",
                "Vakit: Fecr-i sâdık (tan yerinin ağarması) ile başlar, güneş doğmadan önce biter.",
                "Güneş doğarken kerahat vakti başlar (yaklaşık 45 dk sürer) ve bu sırada namaz kılınmaz; bu yüzden sabah namazı güneş doğmadan kılınmalıdır. "
                        + "Sabahın sünneti, sünnetlerin en kuvvetlisidir.",
                Arrays.asList(
                        new Part("Sabahın sünneti", 2, "Müekkede sünnet", iki,
                                ids("subhaneke", "fatiha", "ihlas", "kevser", "fil", "felak", "nas", "ettehiyyatu", "salli_barik", "rabbena")),
                        new Part("Sabahın farzı", 2, "Farz", iki,
                                ids("subhaneke", "fatiha", "ihlas", "kevser", "fil", "felak", "nas", "ettehiyyatu", "salli_barik", "rabbena", "tesbihat", "ayetel_kursi"))
                )));

        PRAYERS.add(new Prayer("ogle", "Öğle namazı",
                "Vakit: Güneş tepe noktasını (zeval) geçince başlar, ikindi vaktine kadar sürür.",
                "Vakit tam zeval anındayken (güneş tepedeyken) namaz kılınmaz; zeval geçince başlar.",
                Arrays.asList(
                        new Part("Öğlenin ilk sünneti", 4, "Müekkede sünnet", dortSunnetMuekkede(),
                                ids("subhaneke", "fatiha", "ihlas", "kevser", "fil", "felak", "nas", "ettehiyyatu", "salli_barik", "rabbena")),
                        new Part("Öğlenin farzı", 4, "Farz", dortFarz(),
                                ids("subhaneke", "fatiha", "ihlas", "kevser", "fil", "felak", "nas", "ettehiyyatu", "salli_barik", "rabbena", "tesbihat", "ayetel_kursi")),
                        new Part("Öğlenin son sünneti", 2, "Müekkede sünnet", iki,
                                ids("subhaneke", "fatiha", "ihlas", "kevser", "fil", "felak", "nas", "ettehiyyatu", "salli_barik", "rabbena"))
                )));

        PRAYERS.add(new Prayer("ikindi", "İkindi namazı",
                "Vakit: Öğle vakti çıkınca başlar, güneş batıncaya kadar sürür.",
                "Güneş sararıp batmaya yaklaşınca (kerahat vakti) o günün ikindi farzı dışında nafile namaz kılınmaz.",
                Arrays.asList(
                        new Part("İkindinin sünneti", 4, "Gayri müekkede sünnet", dortSunnetGayri(),
                                ids("subhaneke", "fatiha", "ihlas", "kevser", "fil", "felak", "nas", "ettehiyyatu", "salli_barik", "rabbena")),
                        new Part("İkindinin farzı", 4, "Farz", dortFarz(),
                                ids("subhaneke", "fatiha", "ihlas", "kevser", "fil", "felak", "nas", "ettehiyyatu", "salli_barik", "rabbena", "tesbihat", "ayetel_kursi"))
                )));

        PRAYERS.add(new Prayer("aksam", "Akşam namazı",
                "Vakit: Güneşin batışıyla başlar, batı ufkundaki kızıllık (şafak) kayboluncaya kadar sürer.",
                "Akşam farzı 3 rekâttır ve gündüz namazlarının vitri sayılır. Vakti kısa olduğu için geciktirilmemesi tavsiye edilir.",
                Arrays.asList(
                        new Part("Akşamın farzı", 3, "Farz", new String[]{
                                "Kamet getirilir, farz niyetiyle tekbir alınır.",
                                "1. rekât: Sübhâneke, Euzü-Besmele, Fâtiha, zamm-ı sûre; rükû, iki secde.",
                                "2. rekât: Besmele, Fâtiha, zamm-ı sûre; rükû, iki secde. İlk oturuşta yalnız Ettehiyyâtü okunur, kalkılır.",
                                "3. rekât: Yalnız Fâtiha okunur, sûre okunmaz; rükû, iki secde. Son oturuşta Ettehiyyâtü, "
                                        + "Allâhümme salli, Allâhümme bârik, Rabbenâ okunur, selam verilir. Ardından tesbihat yapılır."
                        }, ids("subhaneke", "fatiha", "ihlas", "kevser", "fil", "felak", "nas", "ettehiyyatu", "salli_barik", "rabbena", "tesbihat", "ayetel_kursi")),
                        new Part("Akşamın son sünneti", 2, "Müekkede sünnet", iki,
                                ids("subhaneke", "fatiha", "ihlas", "kevser", "fil", "felak", "nas", "ettehiyyatu", "salli_barik", "rabbena"))
                )));

        PRAYERS.add(new Prayer("yatsi", "Yatsı namazı",
                "Vakit: Kızıllık kaybolunca (şafağın bitmesiyle) başlar, imsak vaktine (fecr-i sâdık) kadar sürer.",
                "Vitir namazı vaciptir; yatsı farzından sonra kılınır. Perşembe günleri yatsıdan önce Selâ okunması yaygın bir uygulamadır.",
                Arrays.asList(
                        new Part("Yatsının ilk sünneti", 4, "Gayri müekkede sünnet", dortSunnetGayri(),
                                ids("subhaneke", "fatiha", "ihlas", "kevser", "fil", "felak", "nas", "ettehiyyatu", "salli_barik", "rabbena")),
                        new Part("Yatsının farzı", 4, "Farz", dortFarz(),
                                ids("subhaneke", "fatiha", "ihlas", "kevser", "fil", "felak", "nas", "ettehiyyatu", "salli_barik", "rabbena", "tesbihat", "ayetel_kursi")),
                        new Part("Yatsının son sünneti", 2, "Müekkede sünnet", iki,
                                ids("subhaneke", "fatiha", "ihlas", "kevser", "fil", "felak", "nas", "ettehiyyatu", "salli_barik", "rabbena")),
                        new Part("Vitir namazı", 3, "Vacip", new String[]{
                                "1. rekât: Sübhâneke, Euzü-Besmele, Fâtiha, zamm-ı sûre; rükû, iki secde.",
                                "2. rekât: Besmele, Fâtiha, zamm-ı sûre; rükû, iki secde. İlk oturuşta Ettehiyyâtü okunur "
                                        + "(Hanefî'de vitir bir selamla, üç rekât bitişik kılınır), kalkılır.",
                                "3. rekât: Fâtiha ve zamm-ı sûre okunduktan sonra rükûya gitmeden \"Allâhü ekber\" denilip eller "
                                        + "kulaklara kaldırılır ve tekrar bağlanır; Kunut duaları (Allâhümme innâ nestein'ük… ve "
                                        + "Allâhümme iyyâke na'büdü…) okunur. Sonra rükû, iki secde, son oturuş ve selam."
                        }, ids("subhaneke", "fatiha", "ihlas", "kevser", "fil", "felak", "nas", "kunut_1", "kunut_2", "ettehiyyatu", "salli_barik", "rabbena"))
                )));

        PRAYERS.add(new Prayer("cuma", "Cuma namazı",
                "Vakit: Öğle vaktinde, cemaatle ve hutbe dinlenerek kılınır.",
                "Cuma farzı 2 rekâttır ve cemaatle kılınır. Hutbe okunurken konuşulmaz, namaz kılınmaz. "
                        + "Cumada öğle namazı kılınmaz; cuma farzı onun yerini tutar.",
                Arrays.asList(
                        new Part("Cumanın ilk sünneti", 4, "Müekkede sünnet", dortSunnetMuekkede(),
                                ids("subhaneke", "fatiha", "ihlas", "kevser", "fil", "felak", "nas", "ettehiyyatu", "salli_barik", "rabbena")),
                        new Part("Cuma farzı (imamla)", 2, "Farz", new String[]{
                                "Hutbe dinlenir, kamet getirilir. İmama uyma niyetiyle tekbir alınır.",
                                "Cemaatle: Sübhâneke okunur, imam sesli Fâtiha ve sûre okur; cemaat sessizce dinler. "
                                        + "Rükû ve secdeler imamla birlikte yapılır. Son oturuşta Ettehiyyâtü, Allâhümme salli, "
                                        + "Allâhümme bârik, Rabbenâ okunur; imamla selam verilir."
                        }, ids("subhaneke", "ettehiyyatu", "salli_barik", "rabbena", "tesbihat", "ayetel_kursi")),
                        new Part("Cumanın son sünneti", 4, "Müekkede sünnet", dortSunnetMuekkede(),
                                ids("subhaneke", "fatiha", "ihlas", "kevser", "fil", "felak", "nas", "ettehiyyatu", "salli_barik", "rabbena"))
                )));

        // ---------------------------------------------------------------- dualar ve sûreler
        DUAS.add(new Dua("subhaneke", "Sübhâneke", "Her namazda ilk rekâtta, tekbirden sonra",
                "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ وَتَبَارَكَ اسْمُكَ وَتَعَالَى جَدُّكَ وَلَا إِلَهَ غَيْرُكَ",
                "Sübhânekellâhümme ve bihamdik, ve tebârekesmük, ve teâlâ ceddük, ve lâ ilâhe gayruk.",
                "Allahım! Seni her türlü eksiklikten uzak tutar, sana hamdederim. Senin adın mübarektir, şanın yücedir. "
                        + "Senden başka ilah yoktur."));

        DUAS.add(new Dua("fatiha", "Fâtiha Sûresi", "Her rekâtta okunur (Euzü-Besmele ile başlanır)",
                "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ\nبِسْمِ اللَّهِ الرَّحْمَنِ الرَّحِيمِ\n"
                        + "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ۝ الرَّحْمَنِ الرَّحِيمِ ۝ مَالِكِ يَوْمِ الدِّينِ ۝ "
                        + "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ۝ اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ۝ "
                        + "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
                "Euzü billâhi mineşşeytânirracîm. Bismillâhirrahmânirrahîm. Elhamdü lillâhi rabbil âlemîn. "
                        + "Errahmânirrahîm. Mâliki yevmiddîn. İyyâke na'büdü ve iyyâke neste'în. İhdinassırâtal müstakîm. "
                        + "Sırâtallezîne en'amte aleyhim gayril mağdûbi aleyhim ve leddâllîn.",
                "Rahman ve Rahim Allah'ın adıyla. Hamd, âlemlerin Rabbi Allah'a mahsustur. O, Rahman'dır, Rahim'dir. "
                        + "Din gününün sahibidir. Yalnız sana kulluk eder, yalnız senden yardım dileriz. Bizi doğru yola ilet; "
                        + "kendilerine nimet verdiklerinin yoluna; gazaba uğrayanların ve sapıkların yoluna değil."));

        DUAS.add(new Dua("ihlas", "İhlâs Sûresi (zamm-ı sûre)", "Fâtiha'dan sonra okunabilecek sûrelerden",
                "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُنْ لَهُ كُفُوًا أَحَدٌ",
                "Kul hüvallâhü ehad. Allâhüssamed. Lem yelid ve lem yûled. Ve lem yekün lehû küfüven ehad.",
                "De ki: O, Allah'tır, tektir. Allah sameddir (her şey O'na muhtaçtır, O hiçbir şeye muhtaç değildir). "
                        + "O doğurmamış ve doğurulmamıştır. Hiçbir şey O'na denk değildir."));

        DUAS.add(new Dua("kevser", "Kevser Sûresi (zamm-ı sûre)", "Fâtiha'dan sonra okunabilecek sûrelerden",
                "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ ۝ فَصَلِّ لِرَبِّكَ وَانْحَرْ ۝ إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ",
                "İnnâ a'taynâkel kevser. Fesalli li rabbike venhar. İnne şânieke hüvel ebter.",
                "Şüphesiz biz sana Kevser'i verdik. Öyleyse Rabbin için namaz kıl ve kurban kes. "
                        + "Asıl soyu kesik olan, sana buğzedendir."));

        DUAS.add(new Dua("ettehiyyatu", "Ettehiyyâtü", "Oturuşlarda (ilk ve son oturuş)",
                "اَلتَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ ۝ اَلسَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ ۝ "
                        + "اَلسَّلَامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّالِحِينَ ۝ "
                        + "أَشْهَدُ أَنْ لَا إِلَهَ إِلَّا اللَّهُ وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ",
                "Ettehiyyâtü lillâhi vessalevâtü vettayyibât. Esselâmü aleyke eyyühennebiyyü ve rahmetullâhi ve berekâtüh. "
                        + "Esselâmü aleynâ ve alâ ibâdillâhissâlihîn. Eşhedü en lâ ilâhe illallâh, ve eşhedü enne Muhammeden "
                        + "abdühû ve rasûlüh.",
                "Dil ile, beden ve mal ile yapılan bütün ibadetler Allah'a mahsustur. Ey Peygamber! Allah'ın selamı, rahmeti ve "
                        + "bereketi senin üzerine olsun. Selam bizim üzerimize ve Allah'ın salih kulları üzerine olsun. Şahitlik ederim ki "
                        + "Allah'tan başka ilah yoktur ve yine şahitlik ederim ki Muhammed O'nun kulu ve elçisidir."));

        DUAS.add(new Dua("salli_barik", "Allâhümme salli / Allâhümme bârik (Salavatlar)", "Son oturuşta, Ettehiyyâtü'nden sonra",
                "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ\n"
                        + "اللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ إِنَّكَ حَمِيدٌ مَجِيدٌ",
                "Allâhümme salli alâ Muhammedin ve alâ âli Muhammed, kemâ salleyte alâ İbrâhîme ve alâ âli İbrâhîm, "
                        + "inneke hamîdün mecîd.\nAllâhümme bârik alâ Muhammedin ve alâ âli Muhammed, kemâ bârekte alâ İbrâhîme "
                        + "ve alâ âli İbrâhîm, inneke hamîdün mecîd.",
                "Allahım! İbrahim'e ve İbrahim'in âline rahmet ettiğin gibi Muhammed'e ve Muhammed'in âline de rahmet et. "
                        + "Şüphesiz sen övülmeye layıksın, şanı yücesin. Allahım! İbrahim'e ve âline bereket verdiğin gibi "
                        + "Muhammed'e ve âline de bereket ver. Şüphesiz sen övülmeye layıksın, şanı yücesin."));

        DUAS.add(new Dua("rabbena", "Rabbenâ duaları", "Son oturuşta, salavatlardan sonra; ardından selam verilir",
                "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ\n"
                        + "رَبَّنَا اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ",
                "Rabbenâ âtinâ fiddünyâ haseneten ve fil âhirati haseneten ve kınâ azâben nâr.\n"
                        + "Rabbenağfirlî ve livâlideyye ve lilmü'minîne yevme yekûmül hısâb.",
                "Rabbimiz! Bize dünyada da iyilik ver, ahirette de iyilik ver ve bizi ateş azabından koru.\n"
                        + "Rabbimiz! Hesabın görüleceği gün beni, annemi babamı ve bütün müminleri bağışla."));

        DUAS.add(new Dua("kunut_1", "Kunut duası 1", "Vitir namazının 3. rekâtında, rükûdan önce",
                "اللَّهُمَّ إِنَّا نَسْتَعِينُكَ وَنَسْتَغْفِرُكَ وَنَسْتَهْدِيكَ وَنُؤْمِنُ بِكَ وَنَتُوبُ إِلَيْكَ وَنَتَوَكَّلُ عَلَيْكَ "
                        + "وَنُثْنِي عَلَيْكَ الْخَيْرَ كُلَّهُ نَشْكُرُكَ وَلَا نَكْفُرُكَ وَنَخْلَعُ وَنَتْرُكُ مَنْ يَفْجُرُكَ",
                "Allâhümme innâ nesteînüke ve nestağfirük, ve nestehdîk, ve nü'minü bike ve netûbü ileyk, ve netevekkelü aleyk, "
                        + "ve nüsnî aleykel hayr, külleh, neşkürüke ve lâ nekfürük, ve nahleu ve netrükü men yefcürük.",
                "Allahım! Senden yardım dileriz, bağışlanma isteriz, doğru yolu göstermeni dileriz. Sana inanır, sana tövbe "
                        + "eder, sana güveniriz. Bütün hayırlarla seni överiz. Sana şükreder, nankörlük etmeyiz. "
                        + "Sana isyan edenlerden uzaklaşır ve onları terk ederiz."));

        DUAS.add(new Dua("kunut_2", "Kunut duası 2", "Vitir namazının 3. rekâtında, birinci kunut duasından sonra",
                "اللَّهُمَّ إِيَّاكَ نَعْبُدُ وَلَكَ نُصَلِّي وَنَسْجُدُ وَإِلَيْكَ نَسْعَى وَنَحْفِدُ نَرْجُو رَحْمَتَكَ "
                        + "وَنَخْشَى عَذَابَكَ إِنَّ عَذَابَكَ بِالْكُفَّارِ مُلْحِقٌ",
                "Allâhümme iyyâke na'büdü ve leke nüsallî ve nescüdü ve ileyke nes'â ve nahfid, ercû rahmeteke ve nahşâ "
                        + "azâbeke inne azâbeke bil küffâri mülhık.",
                "Allahım! Yalnız sana kulluk ederiz, senin için namaz kılar ve secde ederiz. Sana koşar, sana yöneliriz. "
                        + "Rahmetini umar, azabından korkarız. Şüphesiz azabın kâfirlere ulaşacaktır."));

        DUAS.add(new Dua("tesbihat", "Namaz sonrası tesbihat", "Farz namazlardan sonra, selamın ardından",
                "سُبْحَانَ اللَّهِ (٣٣) ۝ الْحَمْدُ لِلَّهِ (٣٣) ۝ اللَّهُ أَكْبَرُ (٣٣)\n"
                        + "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
                "Sübhânallâh (33), Elhamdülillâh (33), Allâhü ekber (33).\nLâ ilâhe illallâhü vahdehû lâ şerîke leh, "
                        + "lehül mülkü ve lehül hamdü ve hüve alâ külli şey'in kadîr.",
                "Allah'ı eksikliklerden uzak tutarım (33). Hamd Allah'adır (33). Allah en büyüktür (33). Allah'tan başka ilah "
                        + "yoktur; O tektir, ortağı yoktur. Mülk O'nundur, hamd O'nadır ve O her şeye kadirdir. "
                        + "(Ardından Ayetel Kürsî okunur.)"));

        DUAS.add(new Dua("ayetel_kursi", "Ayetel Kürsî", "Farz namazlardan sonra, tesbihatla birlikte",
                "اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ "
                        + "مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ "
                        + "وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ "
                        + "وَلَا يَئُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ",
                "Allâhü lâ ilâhe illâ hüvel hayyül kayyûm. Lâ te'huzühû sinetün ve lâ nevm. Lehû mâ fîssemâvâti ve mâ fîl ard. "
                        + "Men zellezî yeşfeu indehû illâ bi iznih. Ya'lemü mâ beyne eydîhim ve mâ halfehüm. Ve lâ yühîtûne bi şey'in "
                        + "min ilmihî illâ bimâ şâ. Vesia kürsiyyühüssemâvâti vel ard. Ve lâ yeûdühû hıfzuhümâ ve hüvel aliyyül azîm.",
                "Allah, kendisinden başka ilah olmayandır; diridir, her şeyi ayakta tutandır. O'nu ne uyuklama tutar ne uyku. "
                        + "Göklerde ve yerde ne varsa O'nundur. İzni olmadan O'nun katında kim şefaat edebilir? Onların önlerindekini ve "
                        + "arkalarındakini bilir. O'nun ilminden, dilediği kadarından başka hiçbir şeyi kavrayamazlar. O'nun kürsüsü "
                        + "gökleri ve yeri kaplamıştır; onları korumak O'na ağır gelmez. O çok yücedir, çok büyüktür."));

        DUAS.add(new Dua("fil", "Fîl Sûresi (zamm-ı sûre)", "Fâtiha'dan sonra okunabilecek sûrelerden",
                "أَلَمْ تَرَ كَيْفَ فَعَلَ رَبُّكَ بِأَصْحَابِ الْفِيلِ ۝ أَلَمْ يَجْعَلْ كَيْدَهُمْ فِي تَضْلِيلٍ ۝ "
                        + "وَأَرْسَلَ عَلَيْهِمْ طَيْرًا أَبَابِيلَ ۝ تَرْمِيهِمْ بِحِجَارَةٍ مِنْ سِجِّيلٍ ۝ فَجَعَلَهُمْ كَعَصْفٍ مَأْكُولٍ",
                "Elem tera keyfe fe'ale rabbüke bi ashâbil fîl. Elem yec'al keydehüm fî tadlîl. Ve ersele aleyhim tayran "
                        + "ebâbîl. Termîhim bihicâratin min siccîl. Fece'alehüm ke'asfin me'kûl.",
                "Rabbinin fil sahiplerine ne yaptığını görmedin mi? Onların hile ve tuzaklarını boşa çıkarmadı mı? "
                        + "Üzerlerine sürü sürü kuşlar gönderdi. Bu kuşlar onlara pişmiş çamurdan taşlar atıyordu. "
                        + "Böylece onları yenilmiş ekin yaprağı gibi yaptı."));

        DUAS.add(new Dua("felak", "Felak Sûresi (zamm-ı sûre)", "Fâtiha'dan sonra okunabilecek sûrelerden",
                "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِنْ شَرِّ مَا خَلَقَ ۝ وَمِنْ شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ "
                        + "وَمِنْ شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِنْ شَرِّ حَاسِدٍ إِذَا حَسَدَ",
                "Kul e'ûzü birabbil felak. Min şerri mâ halak. Ve min şerri gâsikın izâ vekab. Ve min şerrin neffâsâti fil "
                        + "ukad. Ve min şerri hâsidin izâ hased.",
                "De ki: Yarattığı şeylerin şerrinden, karanlığı çöktüğü zaman gecenin şerrinden, düğümlere üfleyenlerin "
                        + "şerrinden ve haset ettiği zaman hasetçinin şerrinden sabahın Rabbine sığınırım."));

        DUAS.add(new Dua("nas", "Nâs Sûresi (zamm-ı sûre)", "Fâtiha'dan sonra okunabilecek sûrelerden",
                "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَهِ النَّاسِ ۝ مِنْ شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ "
                        + "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ",
                "Kul e'ûzü birabbin nâs. Melikin nâs. İlâhin nâs. Min şerril vesvâsil hannâs. Ellezî yüvesvisü fî sudûrin "
                        + "nâs. Minel cinneti ven nâs.",
                "De ki: İnsanların Rabbine, insanların Melik'ine, insanların İlahı'na sığınırım; insanların kalplerine "
                        + "vesvese veren sinsi vesvesecinin şerrinden; cinlerden de insanlardan da."));

        DUAS.add(new Dua("ezan_duasi", "Ezan duası", "Ezan bittikten sonra",
                "اللَّهُمَّ رَبَّ هَذِهِ الدَّعْوَةِ التَّامَّةِ وَالصَّلَاةِ الْقَائِمَةِ آتِ مُحَمَّدًا الْوَسِيلَةَ وَالْفَضِيلَةَ "
                        + "وَابْعَثْهُ مَقَامًا مَحْمُودًا الَّذِي وَعَدْتَهُ",
                "Allâhümme rabbe hâzihid da'vetit tâmmeh, vessalâtil kâimeh, âti Muhammedenil vesîlete vel fadîleh, "
                        + "vebashü makâmen mahmûdenillezî vaadteh.",
                "Ey bu eksiksiz davetin ve kılınacak namazın Rabbi olan Allahım! Muhammed'e vesîle ve fazîleti ver, "
                        + "onu vaat ettiğin makâm-ı mahmûd'a ulaştır."));
    }

    private static String[] ids(String... a) {
        return a;
    }

    static Prayer prayer(String key) {
        for (Prayer p : PRAYERS) if (p.key.equals(key)) return p;
        return PRAYERS.get(0);
    }

    static Dua dua(String id) {
        for (Dua d : DUAS) if (d.id.equals(id)) return d;
        return null;
    }
}
