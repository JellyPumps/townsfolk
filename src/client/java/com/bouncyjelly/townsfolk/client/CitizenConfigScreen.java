package com.bouncyjelly.townsfolk.client;

import com.bouncyjelly.townsfolk.citizen.CitizenConfigData;
import com.bouncyjelly.townsfolk.config.ClockTime;
import com.bouncyjelly.townsfolk.config.ConfigContext;
import com.bouncyjelly.townsfolk.config.ConfigOptions;
import com.bouncyjelly.townsfolk.network.SaveConfigPayload;
import com.bouncyjelly.townsfolk.schedule.Activity;
import com.bouncyjelly.townsfolk.schedule.Schedule;
import com.bouncyjelly.townsfolk.schedule.ScheduleEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.*;
import java.util.function.Consumer;

public class CitizenConfigScreen extends Screen {

    private static final int FIELD_W = 260;
    private static final int GRAY = 0xA0A0A0;

    private record LabelAt(String text, int x, int y) {}

    private static final class Row {
        String start, end;
        Activity activity;
        Row(String start, String end, Activity activity) {
            this.start = start; this.end = end; this.activity = activity;
        }
    }

    private final int entityId;
    private final ConfigContext context;
    
    private String name;
    private int skin;
    private String age_group;
    private String personality;
    private String home_id;
    private String work_id;
    private String wallet;
    private final Map<String, String> settings;
    private boolean customSchedule;
    private final List<Row> rows = new ArrayList<>();

    private final List<LabelAt> labels = new ArrayList<>();
    private ConfigOptions.Tab tab = ConfigOptions.Tab.GENERAL;
    private String footer = "";
    private int footerColor = GRAY;

    public CitizenConfigScreen(int entityId, CitizenConfigData data, ConfigContext context) {
        super(Text.literal("Configure Citizen"));
        this.entityId = entityId;
        this.context = context;
        this.name = data.name();
        this.skin = data.skin();
        this.age_group = data.age_group();
        this.personality = data.personality();
        this.home_id = data.home_id().orElse("");
        this.work_id = data.work_id().orElse("");
        this.wallet = Integer.toString(data.wallet());
        this.settings = new HashMap<>(data.settings());
        if (data.scheduleOverride().isPresent()) {
            customSchedule = true;
            loadRows(data.scheduleOverride().get());
        }
    }

    @Override
    public boolean shouldPause() { return false; }

    private void loadRows(Schedule s) {
        rows.clear();
        for (ScheduleEntry e : s.entries()) {
            rows.add(new Row(ClockTime.format(e.start()), ClockTime.format(e.end()), e.activity()));
        }
    }
    private void initPlaces(int x, int y) {
        List<String> homes = new ArrayList<>(List.of(""));
        homes.addAll(context.homeIds());
        addDrawableChild(cycle("Home", homes, home_id, x, y, v -> home_id = v));

        List<String> works = new ArrayList<>(List.of(""));
        works.addAll(context.workIds());
        addDrawableChild(cycle("Work", works, work_id, x, y + 24, v -> work_id = v));
    }

    private void initPersonality(int x, int y) {
        List<String> options = new ArrayList<>(List.of(""));
        options.addAll(ConfigOptions.PERSONALITIES);
        addDrawableChild(cycle("Personality", options, personality, x, y, v -> personality = v));
    }

    private void initPlanned(int x, int y, ConfigOptions.Tab t) {
        for (ConfigOptions.PlannedField f : ConfigOptions.PLANNED) {
            if (f.tab() != t) continue;
            if (f.isChoice()) {
                String current = settings.getOrDefault(f.key(), f.options().get(0));
                addDrawableChild(cycle(f.label(), f.options(), current, x, y, v -> settings.put(f.key(), v)));
                y += 24;
            } else {
                y = textField(f.label(), x, y, settings.getOrDefault(f.key(), ""), 24, "[A-Za-z0-9 _'-]*",
                        s -> { if (s.isBlank()) settings.remove(f.key()); else settings.put(f.key(), s.trim()); });
            }
        }
    }

    private void initSchedule(int x, int y) {
        if (!customSchedule) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Customize schedule"), b -> {
                loadRows(context.defaultSchedule());
                customSchedule = true;
                clearAndInit();
            }).dimensions(x, y, FIELD_W, 20).build());
            return;
        }

        labels.add(new LabelAt("Start", x, y - 12));
        labels.add(new LabelAt("End", x + 54, y - 12));
        labels.add(new LabelAt("Activity", x + 108, y - 12));

        for (int i = 0; i < rows.size(); i++) {
            final int index = i;
            Row row = rows.get(i);
            int ry = y + i * 22;

            TextFieldWidget start = new TextFieldWidget(textRenderer, x, ry, 50, 20, Text.literal("Start"));
            start.setMaxLength(5);
            start.setText(row.start);
            start.setChangedListener(s -> row.start = s);
            addDrawableChild(start);

            TextFieldWidget end = new TextFieldWidget(textRenderer, x + 54, ry, 50, 20, Text.literal("End"));
            end.setMaxLength(5);
            end.setText(row.end);
            end.setChangedListener(s -> row.end = s);
            addDrawableChild(end);

            addDrawableChild(CyclingButtonWidget.<Activity>builder(a -> Text.literal(ConfigOptions.pretty(a.asString())))
                    .values(Activity.values())
                    .initially(row.activity)
                    .omitKeyText()
                    .build(x + 108, ry, 100, 20, Text.literal("Activity"), (btn, v) -> row.activity = v));

            addDrawableChild(ButtonWidget.builder(Text.literal("X"), b -> {
                rows.remove(index);
                clearAndInit();
            }).dimensions(x + 212, ry, 20, 20).build());
        }

        int by = y + rows.size() * 22 + 4;
        ButtonWidget add = ButtonWidget.builder(Text.literal("Add entry"), b -> {
            rows.add(new Row("06:00", "07:00", Activity.HOME));
            clearAndInit();
        }).dimensions(x, by, 100, 20).build();
        add.active = rows.size() < ConfigOptions.MAX_SCHEDULE_ENTRIES;
        addDrawableChild(add);

        addDrawableChild(ButtonWidget.builder(Text.literal("Use default"), b -> {
            customSchedule = false;
            rows.clear();
            clearAndInit();
        }).dimensions(x + 104, by, 100, 20).build());
    }

    @Override
    protected void init() {
        labels.clear();
        ConfigOptions.Tab[] tabs = ConfigOptions.Tab.values();
        int tabW = Math.min(84, (width - 20) / tabs.length);
        int tx = width / 2 - (tabW * tabs.length) / 2;
        for (ConfigOptions.Tab t : tabs) {
            ButtonWidget b = ButtonWidget.builder(Text.literal(t.label), btn -> { tab = t; clearAndInit(); })
                    .dimensions(tx, 8, tabW, 20).build();
            b.active = t != tab;
            addDrawableChild(b);
            tx += tabW;
        }

        int x = width / 2 - FIELD_W / 2;
        switch (tab) {
            case GENERAL -> initGeneral(x, 50);
            case PLACES -> initPlaces(x, 50);
            case SCHEDULE -> initSchedule(x, 70);
            case PERSONALITY -> initPersonality(x, 50);
            case SOCIAL -> initPlanned(x, 52, ConfigOptions.Tab.SOCIAL);
            case LIFE -> initPlanned(x, 52, ConfigOptions.Tab.LIFE);
            case STATUS -> {}
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> save())
                .dimensions(width / 2 - 102, height - 28, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close())
                .dimensions(width / 2 + 2, height - 28, 100, 20).build());
    }

    // ---- widget helpers ----
    private CyclingButtonWidget<String> cycle(String label, List<String> values, String current,
                                              int x, int y, Consumer<String> onChange) {
        String initial = values.contains(current) ? current : values.get(0);
        if (!values.contains(current)) onChange.accept(initial);
        return CyclingButtonWidget.<String>builder(v -> Text.literal(ConfigOptions.pretty(v)))
                .values(values)
                .initially(initial)
                .build(x, y, FIELD_W, 20, Text.literal(label), (btn, v) -> onChange.accept(v));
    }

    private int textField(String label, int x, int y, String initial, int maxLen,
                          String regex, Consumer<String> onChange) {
        labels.add(new LabelAt(label, x, y));
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y + 11, FIELD_W, 20, Text.literal(label));
        f.setMaxLength(maxLen);
        f.setText(initial);
        f.setTextPredicate(s -> s.matches(regex));
        f.setChangedListener(onChange);
        addDrawableChild(f);
        return y + 34;
    }

    private int drawWrapped(DrawContext ctx, String text, int x, int y, int w, int color) {
        for (OrderedText line : textRenderer.wrapLines(Text.literal(text), w)) {
            ctx.drawTextWithShadow(textRenderer, line, x, y, color);
            y += 10;
        }
        return y;
    }

    // ---- tabs ----
    private void initGeneral(int x, int y) {
        y = textField("Name", x, y, name, 24, "[A-Za-z0-9_-]*", s -> name = s);
        addDrawableChild(cycle("Age group (planned)", ConfigOptions.AGE_GROUPS, age_group, x, y, v -> age_group = v));
        y += 24;
        List<String> skins = new ArrayList<>();
        for (int i = 0; i < ConfigOptions.SKIN_VARIANTS; i++) skins.add(Integer.toString(i));
        addDrawableChild(cycle("Skin variant (planned)", skins, Integer.toString(skin), x, y, v -> skin = Integer.parseInt(v)));
        y += 24;
        textField("Wallet (stored, not used yet)", x, y, wallet, 7, "\\d*", s -> wallet = s);
    }

    // ---- saving ----
    private void fail(String message) {
        footer = message;
        footerColor = 0xFF5555;
    }

    public void onResult(boolean ok, String message) {
        footer = message;
        footerColor = ok ? 0x55FF55 : 0xFF5555;
    }

    private void save() {
        if (name.isEmpty()) { fail("Name can't be empty."); return; }

        int walletValue = 0;
        if (!wallet.isEmpty()) {
            try { walletValue = Integer.parseInt(wallet); }
            catch (NumberFormatException e) { fail("Wallet must be a number."); return; }
        }
        if (walletValue > 1_000_000) { fail("Wallet can be at most 1,000,000."); return; }

        Optional<Schedule> schedule = Optional.empty();
        if (customSchedule) {
            if (rows.isEmpty()) { fail("Add a schedule entry, or switch back to the default."); return; }
            List<ScheduleEntry> entries = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                Row r = rows.get(i);
                int s = ClockTime.parse(r.start);
                int e = ClockTime.parse(r.end);
                if (s < 0 || e < 0) { fail("Schedule row " + (i + 1) + ": use times like 07:30."); return; }
                if (e == 0) e = 24000;  // "06:00" as an end time means the end of the day
                entries.add(new ScheduleEntry(s, e, r.activity));
            }
            Schedule built = new Schedule(entries);
            List<String> problems = built.problems();
            if (!problems.isEmpty()) { fail("Schedule: " + problems.get(0)); return; }
            schedule = Optional.of(built);
        }

        CitizenConfigData data = new CitizenConfigData(name, skin, age_group, personality,
                home_id.isEmpty() ? Optional.empty() : Optional.of(home_id),
                work_id.isEmpty() ? Optional.empty() : Optional.of(work_id),
                walletValue, schedule, Map.copyOf(settings));
        ClientPlayNetworking.send(new SaveConfigPayload(entityId, data));
        footer = "Saving...";
        footerColor = GRAY;
    }

    // ---- drawing ----
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int x = width / 2 - FIELD_W / 2;

        for (LabelAt l : labels) ctx.drawTextWithShadow(textRenderer, l.text(), l.x(), l.y(), GRAY);

        if (tab == ConfigOptions.Tab.PERSONALITY) {
            String blurb = personality.isEmpty() ? "No personality set."
                    : ConfigOptions.PERSONALITY_BLURBS.getOrDefault(personality, "");
            drawWrapped(ctx, blurb, x, 80, FIELD_W, GRAY);
        }

        if (tab == ConfigOptions.Tab.SCHEDULE) {
            drawWrapped(ctx, customSchedule
                            ? "24-hour clock, day starts at 06:00. Entries can't cross midnight, so split them."
                            : "Using the default schedule. Customize to edit it for this citizen only.",
                    x, 34, FIELD_W, GRAY);
        }

        if (tab == ConfigOptions.Tab.STATUS) {
            int y = drawWrapped(ctx, name, x, 40, FIELD_W, 0xFFFFFF);
            y = drawWrapped(ctx, context.status(), x, y + 6, FIELD_W, GRAY);
            drawWrapped(ctx, "Snapshot from when this screen opened. Close and reopen to refresh.", x, y + 10, FIELD_W, 0x808080);
        }

        if (tab == ConfigOptions.Tab.PLACES) {
            drawWrapped(ctx, "Places are created in the world with /townsfolk place add.", x, 108, FIELD_W, GRAY);
        }

        ctx.drawCenteredTextWithShadow(textRenderer, footer, width / 2, height - 42, footerColor);
    }
}