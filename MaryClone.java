import java.util.*;

public class MaryClone {

    // シーンデータの定義
    static class Scene {
        String id;
        String title;
        String description;
        List<String> baseCommands;
        Map<String, Outcome> outcomes = new HashMap<>();

        Scene(String id, String title, String description, String... commands) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.baseCommands = Arrays.asList(commands);
        }

        Scene add(String modifiedWord, String message, int hpChange, String nextSceneId, String endingName) {
            outcomes.put(modifiedWord, new Outcome(message, hpChange, nextSceneId, endingName));
            return this;
        }
    }

    // コマンド実行結果の定義
    static class Outcome {
        String message;
        int hpChange;
        String nextSceneId;
        String endingName; // nullでなければエンディング到達

        Outcome(String message, int hpChange, String nextSceneId, String endingName) {
            this.message = message;
            this.hpChange = hpChange;
            this.nextSceneId = nextSceneId;
            this.endingName = endingName;
        }
    }

    static int hp = 100;
    static Set<String> unlockedEndings = new LinkedHashSet<>();
    static final int TOTAL_ENDINGS = 8;
    static Map<String, Scene> scenes = new HashMap<>();

    public static void main(String[] args) {
        initScenes();
        Scanner scanner = new Scanner(System.in);

        System.out.println("==========================================");
        System.out.println("   MARY - メアリ姫の奪還 (Java再現版)   ");
        System.out.println("==========================================");

        while (true) {
            hp = 100;
            String currentSceneId = "PROLOGUE";

            while (currentSceneId != null) {
                Scene scene = scenes.get(currentSceneId);
                System.out.println("\n------------------------------------------");
                System.out.printf("【%s】  [勇者HP: %d / 100]  [回収ED: %d/%d]\n",
                        scene.title, hp, unlockedEndings.size(), TOTAL_ENDINGS);
                System.out.println("------------------------------------------");
                System.out.println(scene.description);
                System.out.println("\n▼ コマンド一覧:");
                for (int i = 0; i < scene.baseCommands.size(); i++) {
                    System.out.printf("  %d: %s\n", i + 1, scene.baseCommands.get(i));
                }

                // コマンド選択
                int cmdIdx = -1;
                while (cmdIdx < 0 || cmdIdx >= scene.baseCommands.size()) {
                    System.out.print("\n番号を選んでください (1〜" + scene.baseCommands.size() + "): ");
                    if (scanner.hasNextInt()) {
                        cmdIdx = scanner.nextInt() - 1;
                    } else {
                        scanner.next();
                    }
                }
                scanner.nextLine(); // 改行消費

                String selectedCmd = scene.baseCommands.get(cmdIdx);
                String finalCmd = promptInsertChar(scanner, selectedCmd);

                System.out.println("\n▶ 勇者の行動: 【 " + finalCmd + " 】");
                Outcome result = scene.outcomes.get(finalCmd);

                if (result != null) {
                    System.out.println(result.message);
                    hp += result.hpChange;
                    if (result.hpChange < 0) {
                        System.out.println("（勇者は " + (-result.hpChange) + " のダメージを受けた！）");
                    } else if (result.hpChange > 0) {
                        System.out.println("（勇者のHPが " + result.hpChange + " 回復した！）");
                    }

                    if (hp <= 0) {
                        System.out.println("\n勇者は力尽きた……。");
                        recordEnding("ED0: 体力切れの末路");
                        currentSceneId = null;
                    } else if (result.endingName != null) {
                        recordEnding(result.endingName);
                        currentSceneId = null;
                    } else {
                        currentSceneId = result.nextSceneId;
                    }
                } else {
                    // 未定義の文字列を作った時の汎用リアクション
                    System.out.println("「" + finalCmd + "」……意味が通じず、世界が困惑している！");
                    System.out.println("隙を突かれて謎のダメージを受けた！");
                    hp -= 25;
                    if (hp <= 0) {
                        System.out.println("\n意味不明な言葉をつぶやきながら勇者は倒れた……。");
                        recordEnding("ED0: 体力切れの末路");
                        currentSceneId = null;
                    }
                }
            }

            System.out.println("\n==========================================");
            System.out.println("回収済みエンディング一覧: " + unlockedEndings);
            System.out.print("もう一度挑戦しますか？ (y/n): ");
            String retry = scanner.nextLine().trim().toLowerCase();
            if (!retry.equals("y")) {
                System.out.println("プレイありがとうございました！");
                break;
            }
        }
    }

    // 文字列の任意の位置に1文字挿入する処理
    private static String promptInsertChar(Scanner scanner, String base) {
        System.out.println("\n選択中: " + renderSlots(base));
        System.out.print("1文字挿入しますか？ 挿入位置(0〜" + base.length() + ")を入力 / そのまま実行なら Enter: ");
        String posInput = scanner.nextLine().trim();

        if (posInput.isEmpty()) {
            return base;
        }

        try {
            int pos = Integer.parseInt(posInput);
            if (pos < 0 || pos > base.length()) {
                System.out.println("位置が範囲外のため、そのまま実行します。");
                return base;
            }
            System.out.print("挿入する【1文字】を入力してください: ");
            String charInput = scanner.nextLine();
            if (charInput.isEmpty()) {
                return base;
            }
            char c = charInput.charAt(0);
            return new StringBuilder(base).insert(pos, c).toString();
        } catch (NumberFormatException e) {
            System.out.println("数値以外が入力されたため、そのまま実行します。");
            return base;
        }
    }

    // 挿入位置のガイド表示（例: [0]門[1]を[2]叩[3]く[4]）
    private static String renderSlots(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            sb.append("[").append(i).append("]").append(text.charAt(i));
        }
        sb.append("[").append(text.length()).append("]");
        return sb.toString();
    }

    private static void recordEnding(String endingName) {
        boolean isNew = unlockedEndings.add(endingName);
        System.out.println("\n★☆★ エンディング到達: 【" + endingName + "】 " + (isNew ? "(NEW!)" : "(回収済)") + " ★☆★");
    }

    // シナリオと分岐の登録
    private static void initScenes() {
        // --- 第1幕：魔王城の門 ---
        Scene s1 = new Scene(
            "PROLOGUE",
            "第1幕：魔王城の正門",
            "メアリ姫が囚われている魔王城の前に着いた。\n巨大な鉄の門が固く閉ざされている。どうする？",
            "門を叩く", "兵と戦う", "帰る"
        );
        // そのまま実行
        s1.add("門を叩く", "ドン！ドン！…音が響き渡り、上から熱湯を浴びせられた！", -40, "PROLOGUE", null);
        s1.add("兵と戦う", "門番のダークナイトは強すぎた！一撃で吹き飛ばされた。", -100, null, null);
        s1.add("帰る", "勇者は使命を放棄して故郷の村へ帰った。平和な日常が戻った（？）。", 0, null, "ED1: 賢明なる撤退");
        // 1文字挿入の正解・ネタ分岐
        s1.add("門を開ける", "なんと鍵がかかっていなかった！勇者は堂々と城内へ侵入した！", 0, "CASTLE_HALL", null);
        s1.add("門を壊す", "勇者の怪力で鉄の門を粉砕！派手な音に驚いて門番は逃げ出した！", -10, "CASTLE_HALL", null);
        s1.add("兵と和む", "世間話を始めたら意気投合し、裏口からこっそり通してもらえた！", 20, "CASTLE_HALL", null);
        s1.add("帰れる", "「帰れる」と確信した瞬間、転移魔法が発動して実家の布団に戻った。", 0, null, "ED2: 帰宅の魔法");
        scenes.put(s1.id, s1);

        // --- 第2幕：玉座の間 ---
        Scene s2 = new Scene(
            "CASTLE_HALL",
            "第2幕：魔王の玉座",
            "最上階にたどり着いた！\n玉座には魔王が座り、その隣には檻に入れられたメアリ姫がいる。\n魔王「よくぞ来た勇者よ！さあ、決着をつけよう！」",
            "魔王を斬る", "姫を救う", "剣を捨てる"
        );
        // そのまま実行
        s2.add("魔王を斬る", "魔王のバリアに弾かれた！反撃のダークフレアを浴びる！", -60, "CASTLE_HALL", null);
        s2.add("姫を救う", "檻に近づいた瞬間、魔王の背後からの不意打ちを食らった！", -70, "CASTLE_HALL", null);
        s2.add("剣を捨てる", "丸腰になった勇者を見て、魔王は呆れながらトドメを刺した。", -100, null, null);
        // 1文字挿入の分岐
        s2.add("魔王を斬れる", "「斬れる」という強い思い込みが聖剣の力を覚醒させた！魔王を一刀両断し、メアリ姫を救出した！", 0, null, "TRUE END: 言葉の勇者");
        s2.add("魔王と斬る", "魔王と共闘して背後から迫っていた真の黒幕を撃破！世界は救われた！", 0, null, "ED3: 芽生えた友情");
        s2.add("姫を殺す", "なんてことを！勇者は闇落ちし、新たな魔王として君臨した……。", 0, null, "ED4: 暗黒の勇者");
        s2.add("剣を売る", "戦闘中に武器屋を呼び出し、聖剣を100万Gで売却！大富豪になった勇者は姫を置いて旅に出た。", 0, null, "ED5: 商魂たくましき勇者");
        scenes.put(s2.id, s2);
    }
}
