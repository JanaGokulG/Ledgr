package com.ledgr.ui;

import com.ledgr.LedgrApplication;
import com.ledgr.config.DotenvLoader;
import com.ledgr.entity.AccountEntity;
import com.ledgr.entity.TransactionEntity;
import com.ledgr.repository.AccountRepository;
import com.ledgr.repository.TransactionRepository;
import com.ledgr.sandbox.controller.FintechToolsController;
import com.ledgr.sandbox.service.FiuIngestionService;
import com.ledgr.sandbox.service.RealtimeTransactionSimulatorService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;

/**
 * Ledgr Native JavaFX Desktop Application
 * Authentic, professional fintech interface for student financial planning.
 * Operates natively with Spring Boot backend services and local database.
 */
public class LedgrDesktopApp extends Application {

    private ConfigurableApplicationContext springContext;
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private FintechToolsController fintechToolsController;
    private FiuIngestionService fiuIngestionService;
    private RealtimeTransactionSimulatorService simulatorService;

    // Global Privacy Hotkey Shield (Ctrl+Shift+H)
    private final SimpleBooleanProperty privacyShieldActive = new SimpleBooleanProperty(false);

    // Active Navigation View
    private final SimpleStringProperty currentView = new SimpleStringProperty("ACCOUNTS");

    // UI Root Containers
    private BorderPane mainLayout;
    private StackPane contentArea;
    private Label topViewTitle;
    private Label topViewSubtitle;
    private Button privacyShieldBtn;
    private Label simulatorStatusPill;

    // Currency Formatter
    private final NumberFormat inrFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    // Periodic UI Refresh Timeline (Polls live DB changes smoothly on UI thread)
    private Timeline liveUpdateTimeline;

    @Override
    public void init() throws Exception {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("stdout.encoding", "UTF-8");
        System.setProperty("stderr.encoding", "UTF-8");
        DotenvLoader.load();
        this.springContext = new SpringApplicationBuilder(LedgrApplication.class)
                .headless(false)
                .run(getParameters().getRaw().toArray(new String[0]));

        this.accountRepository = springContext.getBean(AccountRepository.class);
        this.transactionRepository = springContext.getBean(TransactionRepository.class);
        this.fintechToolsController = springContext.getBean(FintechToolsController.class);
        this.fiuIngestionService = springContext.getBean(FiuIngestionService.class);
        this.simulatorService = springContext.getBean(RealtimeTransactionSimulatorService.class);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Ledgr — Student Financial Intelligence & Protection");
        primaryStage.setMinWidth(1180);
        primaryStage.setMinHeight(760);

        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("app-container");

        // 1. Sidebar Navigation
        VBox sidebar = buildSidebar();
        mainLayout.setLeft(sidebar);

        // 2. Top Header Bar
        HBox topHeader = buildTopHeader();
        mainLayout.setTop(topHeader);

        // 3. Central Dynamic Content Area
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-pane");
        mainLayout.setCenter(contentArea);

        // Load initial view
        navigateTo("ACCOUNTS");

        Scene scene = new Scene(mainLayout, 1260, 820);
        String cssPath = getClass().getResource("/styles/ledgr-dark.css") != null
                ? getClass().getResource("/styles/ledgr-dark.css").toExternalForm() : null;
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }

        // Global Privacy Hotkey Shield: Ctrl+Shift+H
        KeyCombination hotkey = new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        scene.setOnKeyPressed(event -> {
            if (hotkey.match(event)) {
                togglePrivacyShield();
                event.consume();
            }
        });

        // Setup smooth background UI update timeline (every 3 seconds)
        liveUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> refreshCurrentViewIfLive()));
        liveUpdateTimeline.setCycleCount(Animation.INDEFINITE);
        liveUpdateTimeline.play();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        if (liveUpdateTimeline != null) {
            liveUpdateTimeline.stop();
        }
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }

    // =========================================================================
    // NAVIGATION & HEADER BUILDERS
    // =========================================================================

    private VBox buildSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        // Header brand
        VBox brandBox = new VBox(2);
        brandBox.getStyleClass().add("sidebar-header");
        HBox titleBox = new HBox(4);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("LEDGR");
        title.getStyleClass().add("brand-title");
        Label dot = new Label("•");
        dot.setStyle("-fx-text-fill: #10b981; -fx-font-size: 20px;");
        titleBox.getChildren().addAll(title, dot);

        Label sub = new Label("STUDENT FIU PLATFORM");
        sub.getStyleClass().add("brand-subtitle");
        brandBox.getChildren().addAll(titleBox, sub);

        // Navigation Items
        VBox navButtons = new VBox(4);
        navButtons.setPadding(new Insets(12, 0, 12, 0));

        Label overviewSection = new Label("FINANCIAL HUB");
        overviewSection.getStyleClass().add("nav-section-label");

        Button btnAccounts = createNavButton("📊  Accounts & Net Worth", "ACCOUNTS");
        Button btnTransactions = createNavButton("📜  Ledger & Transactions", "TRANSACTIONS");

        Label toolsSection = new Label("BANKING PROTECTION");
        toolsSection.getStyleClass().add("nav-section-label");

        Button btnRenewals = createNavButton("⏰  Auto-Renewal Warnings", "RENEWALS");
        Button btnShield = createNavButton("🛡️  Minimum Balance Shield", "SHIELD");

        Label studentSection = new Label("PLANNING & TOOLS");
        studentSection.getStyleClass().add("nav-section-label");

        Button btnRunway = createNavButton("🎓  Scholarship & Salary CTC", "STUDENT_TOOLS");
        Button btnSimulator = createNavButton("⚡  AA Gateway & Simulator", "SIMULATOR");

        navButtons.getChildren().addAll(
                overviewSection, btnAccounts, btnTransactions,
                toolsSection, btnRenewals, btnShield,
                studentSection, btnRunway, btnSimulator
        );

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer User Profile (loaded dynamically from database)
        VBox footer = new VBox(4);
        footer.getStyleClass().add("sidebar-footer");
        AccountEntity profileAcc = accountRepository != null ? accountRepository.findAll().stream()
                .filter(a -> a.getHolderName() != null && !a.getHolderName().isBlank())
                .findFirst().orElse(null) : null;
        String holder = profileAcc != null ? profileAcc.getHolderName() : "Akshay Kumar";
        String pan = profileAcc != null && profileAcc.getHolderPan() != null ? profileAcc.getHolderPan() : "AIMPN5746M";
        boolean ckyc = profileAcc != null && Boolean.TRUE.equals(profileAcc.getCkycCompliant());
        Label userLabel = new Label(holder);
        userLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #f1f5f9; -fx-font-size: 12px;");
        Label panLabel = new Label("PAN: " + pan + "  \u2022  " + (ckyc ? "KYC Validated" : "KYC Pending"));
        panLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 10px;");
        Label hotkeyHint = new Label("Hotkey: Ctrl+Shift+H (Privacy)");
        hotkeyHint.setStyle("-fx-text-fill: #38bdf8; -fx-font-size: 10px; -fx-font-weight: 600;");
        footer.getChildren().addAll(userLabel, panLabel, hotkeyHint);

        sidebar.getChildren().addAll(brandBox, navButtons, spacer, footer);
        return sidebar;
    }

    private Button createNavButton(String text, String viewId) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("nav-button");

        currentView.addListener((obs, oldVal, newVal) -> {
            if (viewId.equals(newVal)) {
                if (!btn.getStyleClass().contains("nav-button-active")) {
                    btn.getStyleClass().add("nav-button-active");
                }
            } else {
                btn.getStyleClass().remove("nav-button-active");
            }
        });

        if (viewId.equals(currentView.get())) {
            btn.getStyleClass().add("nav-button-active");
        }

        btn.setOnAction(e -> navigateTo(viewId));
        return btn;
    }

    private HBox buildTopHeader() {
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("top-header");

        VBox titleCol = new VBox(2);
        topViewTitle = new Label("Accounts & Net Worth");
        topViewTitle.getStyleClass().add("view-title");
        topViewSubtitle = new Label("Real-time balance breakdown across verified bank deposits");
        topViewSubtitle.getStyleClass().add("view-subtitle");
        titleCol.getChildren().addAll(topViewTitle, topViewSubtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Live Simulator Pill
        simulatorStatusPill = new Label("● STREAMING");
        simulatorStatusPill.getStyleClass().addAll("badge", "badge-emerald");

        // Privacy Shield Toggle Button
        privacyShieldBtn = new Button("🛡️ Privacy: OFF");
        privacyShieldBtn.getStyleClass().add("button-secondary");
        privacyShieldBtn.setOnAction(e -> togglePrivacyShield());

        // Sync ReBIT AA Button
        Button syncBtn = new Button("🔄 Sync AA Gateway");
        syncBtn.getStyleClass().add("button-secondary");
        syncBtn.setOnAction(e -> handleSyncAAGateway());

        header.getChildren().addAll(titleCol, spacer, simulatorStatusPill, privacyShieldBtn, syncBtn);
        return header;
    }

    private void togglePrivacyShield() {
        boolean nextState = !privacyShieldActive.get();
        privacyShieldActive.set(nextState);
        if (nextState) {
            privacyShieldBtn.setText("🛡️ Privacy: ON (Ctrl+Shift+H)");
            privacyShieldBtn.setStyle("-fx-background-color: #064e3b; -fx-text-fill: #34d399; -fx-border-color: #059669;");
        } else {
            privacyShieldBtn.setText("🛡️ Privacy: OFF");
            privacyShieldBtn.setStyle("");
        }
        navigateTo(currentView.get());
    }

    private void navigateTo(String viewId) {
        currentView.set(viewId);
        contentArea.getChildren().clear();

        Node viewContent;
        switch (viewId) {
            case "ACCOUNTS":
                topViewTitle.setText("Accounts & Net Worth");
                topViewSubtitle.setText("Local multi-account overview verified via RBI Account Aggregator");
                viewContent = buildAccountsView();
                break;
            case "TRANSACTIONS":
                topViewTitle.setText("Live Ledger & Transactions");
                topViewSubtitle.setText("Continuous stream of categorized financial transactions");
                viewContent = buildTransactionsView();
                break;
            case "RENEWALS":
                topViewTitle.setText("Auto-Renewal Early Warnings");
                topViewSubtitle.setText("Proactive tracking of upcoming recurring auto-debits with 7-day advance warnings");
                viewContent = buildAutoRenewalsView();
                break;
            case "SHIELD":
                topViewTitle.setText("Minimum Balance Shield");
                topViewSubtitle.setText("Dedicated bank account protection preventing Minimum Average Balance (MAB) breaches");
                viewContent = buildMinBalanceShieldView();
                break;
            case "STUDENT_TOOLS":
                topViewTitle.setText("Scholarship Runway & CTC Sandbox");
                topViewSubtitle.setText("Personalized runway simulation and post-grad salary take-home calculator");
                viewContent = buildStudentToolsView();
                break;
            case "SIMULATOR":
                topViewTitle.setText("AA Sandbox & Realtime Simulator");
                topViewSubtitle.setText("Trigger ReBIT AA multi-party consent handshakes or inject test transactions");
                viewContent = buildSimulatorView();
                break;
            default:
                viewContent = new Label("View not found");
        }

        ScrollPane scrollPane = new ScrollPane(viewContent);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        contentArea.getChildren().add(scrollPane);
    }

    private void refreshCurrentViewIfLive() {
        // Refresh lightweight views if active
        String cur = currentView.get();
        if ("ACCOUNTS".equals(cur) || "TRANSACTIONS".equals(cur)) {
            navigateTo(cur);
        }
    }

    private String formatMoney(BigDecimal amount) {
        if (privacyShieldActive.get()) {
            return "\u20B9 \u2022\u2022\u2022\u2022\u2022\u2022";
        }
        if (amount == null) return "\u20B90.00";
        return "\u20B9" + String.format(Locale.US, "%,.2f", amount);
    }

    // =========================================================================
    // VIEW 1: ACCOUNTS & NET WORTH
    // =========================================================================

    private Node buildAccountsView() {
        VBox root = new VBox(20);
        root.setFillWidth(true);

        List<AccountEntity> accounts = accountRepository.findAll();

        BigDecimal liquidSavings = BigDecimal.ZERO;
        BigDecimal fdAssets = BigDecimal.ZERO;
        BigDecimal rdAssets = BigDecimal.ZERO;

        for (AccountEntity acc : accounts) {
            BigDecimal bal = acc.getCurrentBalance() != null ? acc.getCurrentBalance() : BigDecimal.ZERO;
            String accType = acc.getAccountType() != null ? acc.getAccountType().toUpperCase() : "";
            String fiType = acc.getFiType() != null ? acc.getFiType().toUpperCase() : "";

            if ("SAVINGS".equals(accType) || "DEPOSIT".equals(fiType)) {
                liquidSavings = liquidSavings.add(bal);
            } else if ("FIXED".equals(accType) || "TERM-DEPOSIT".equals(fiType)) {
                BigDecimal fdVal = (acc.getMaturityAmount() != null && acc.getMaturityAmount().compareTo(BigDecimal.ZERO) > 0)
                        ? acc.getMaturityAmount() : bal;
                fdAssets = fdAssets.add(fdVal);
            } else if ("RECURRING".equals(accType) || "RECURRING_DEPOSIT".equals(fiType)) {
                rdAssets = rdAssets.add(bal);
            }
        }

        BigDecimal netWorth = liquidSavings.add(fdAssets).add(rdAssets);

        // Top Summary Metric Cards Grid
        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(16);
        metricsGrid.setVgap(16);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(25);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(25);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(25);
        metricsGrid.getColumnConstraints().addAll(col1, col2, col3, col4);

        VBox cardNetWorth = createMetricCard("TOTAL NET WORTH", formatMoney(netWorth), "Liquid Savings + Fixed + Recurring Deposits", "badge-emerald");
        VBox cardLiquid = createMetricCard("LIQUID SAVINGS", formatMoney(liquidSavings), "Readily spendable across linked bank accounts", "badge-blue");
        VBox cardTerm = createMetricCard("FIXED DEPOSIT BUFFER", formatMoney(fdAssets), "Guaranteed term yield (7.1% p.a.)", "badge-purple");
        VBox cardRD = createMetricCard("RECURRING DEPOSITS", formatMoney(rdAssets), "Monthly student savings compounding", "badge-slate");

        metricsGrid.add(cardNetWorth, 0, 0);
        metricsGrid.add(cardLiquid, 1, 0);
        metricsGrid.add(cardTerm, 2, 0);
        metricsGrid.add(cardRD, 3, 0);

        // Account Details Section
        VBox accountsSection = new VBox(12);
        Label sectionTitle = new Label("LINKED BANK ACCOUNTS (REBIT AA VERIFIED)");
        sectionTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-letter-spacing: 1px;");
        accountsSection.getChildren().add(sectionTitle);

        for (AccountEntity acc : accounts) {
            accountsSection.getChildren().add(buildAccountCard(acc));
        }

        root.getChildren().addAll(metricsGrid, accountsSection);
        return root;
    }

    private VBox createMetricCard(String title, String value, String subtext, String badgeClass) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);
        Label t = new Label(title);
        t.getStyleClass().add("card-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label badge = new Label("VERIFIED");
        badge.getStyleClass().addAll("badge", badgeClass);
        top.getChildren().addAll(t, sp, badge);

        Label val = new Label(value);
        val.getStyleClass().add("metric-value");

        Label sub = new Label(subtext);
        sub.getStyleClass().add("metric-subtext");

        card.getChildren().addAll(top, val, sub);
        return card;
    }

    private VBox buildAccountCard(AccountEntity acc) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("card", "card-highlight");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        String accType = acc.getAccountType() != null ? acc.getAccountType().toUpperCase() : "SAVINGS";
        Label typeBadge = new Label(accType);
        typeBadge.getStyleClass().addAll("badge", "SAVINGS".equals(accType) ? "badge-emerald" : ("FIXED".equals(accType) ? "badge-purple" : "badge-amber"));

        Label accNumber = new Label(acc.getAccountNumber() != null ? acc.getAccountNumber() : "SB-UNKNOWN");
        accNumber.setStyle("-fx-font-weight: 700; -fx-font-size: 15px; -fx-text-fill: #f8fafc;");

        Label branch = new Label(acc.getBranchName() != null ? "• " + acc.getBranchName() : "");
        branch.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        BigDecimal bal = acc.getCurrentBalance() != null ? acc.getCurrentBalance() : BigDecimal.ZERO;
        Label balanceLabel = new Label(formatMoney(bal));
        balanceLabel.setStyle("-fx-font-weight: 800; -fx-font-size: 18px; -fx-text-fill: #f8fafc;");

        header.getChildren().addAll(typeBadge, accNumber, branch, sp, balanceLabel);

        // Details Row
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(6);

        addAccountDetail(grid, 0, 0, "IFSC", acc.getIfsc() != null ? acc.getIfsc() : "ICIC0001124");
        addAccountDetail(grid, 1, 0, "Holder", acc.getHolderName() != null ? acc.getHolderName() : "Akshay Kumar");
        addAccountDetail(grid, 2, 0, "Nominee", acc.getNomineeStatus() != null ? acc.getNomineeStatus() : "REGISTERED");

        if ("FIXED".equalsIgnoreCase(accType)) {
            addAccountDetail(grid, 0, 1, "Interest Rate", (acc.getInterestRate() != null ? acc.getInterestRate() : BigDecimal.ZERO) + "% p.a.");
            addAccountDetail(grid, 1, 1, "Maturity Amount", formatMoney(acc.getMaturityAmount()));
            addAccountDetail(grid, 2, 1, "Maturity Date", acc.getMaturityDate() != null ? acc.getMaturityDate() : "N/A");
        } else if ("RECURRING".equalsIgnoreCase(accType)) {
            addAccountDetail(grid, 0, 1, "Monthly Deposit", formatMoney(acc.getRecurringAmount()));
            addAccountDetail(grid, 1, 1, "Due Day of Month", (acc.getRecurringDepositDay() != null ? acc.getRecurringDepositDay() : 1) + "th");
            addAccountDetail(grid, 2, 1, "Maturity Value", formatMoney(acc.getMaturityAmount()));
        } else {
            addAccountDetail(grid, 0, 1, "Status", acc.getStatus() != null ? acc.getStatus() : "ACTIVE");
            addAccountDetail(grid, 1, 1, "Currency", acc.getCurrency() != null ? acc.getCurrency() : "INR");
            addAccountDetail(grid, 2, 1, "CKYC Compliant", Boolean.TRUE.equals(acc.getCkycCompliant()) ? "Yes (Verified)" : "Pending");
        }

        card.getChildren().addAll(header, grid);
        return card;
    }

    private void addAccountDetail(GridPane grid, int col, int row, String label, String value) {
        VBox box = new VBox(2);
        Label l = new Label(label.toUpperCase());
        l.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: #475569;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #cbd5e1;");
        box.getChildren().addAll(l, v);
        grid.add(box, col, row);
    }

    // =========================================================================
    // VIEW 2: TRANSACTIONS & LIVE LEDGER
    // =========================================================================

    private Node buildTransactionsView() {
        VBox root = new VBox(16);
        root.setFillWidth(true);

        List<TransactionEntity> allTxns = transactionRepository.findAll();
        // Sort descending by timestamp
        allTxns.sort((a, b) -> {
            String tsA = a.getTransactionTimestamp() != null ? a.getTransactionTimestamp() : "";
            String tsB = b.getTransactionTimestamp() != null ? b.getTransactionTimestamp() : "";
            return tsB.compareTo(tsA);
        });

        // Filter and Search Toolbar
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by merchant, narration, or category...");
        searchField.setPrefWidth(320);

        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All Types", "DEBIT", "CREDIT");
        typeFilter.setValue("All Types");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label countLabel = new Label(allTxns.size() + " Total Transactions Recorded");
        countLabel.setStyle("-fx-text-fill: #64748b; -fx-font-weight: 600;");

        Button triggerTxnBtn = new Button("⚡ Inject Live Transaction");
        triggerTxnBtn.getStyleClass().add("button-primary");
        triggerTxnBtn.setOnAction(e -> {
            simulatorService.generateAndPersistTransaction(null);
            navigateTo("TRANSACTIONS");
        });

        toolbar.getChildren().addAll(searchField, typeFilter, sp, countLabel, triggerTxnBtn);

        // Transactions TableView
        TableView<TransactionEntity> table = new TableView<>();
        table.setPrefHeight(520);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<TransactionEntity, String> dateCol = new TableColumn<>("TIMESTAMP");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTransactionTimestamp() != null ? c.getValue().getTransactionTimestamp() : ""));
        dateCol.setPrefWidth(140);

        TableColumn<TransactionEntity, String> narrationCol = new TableColumn<>("NARRATION & PAYEE");
        narrationCol.setCellValueFactory(c -> {
            String narr = c.getValue().getNarration() != null ? c.getValue().getNarration() : "";
            String payee = c.getValue().getMerchantPayee() != null ? " [" + c.getValue().getMerchantPayee() + "]" : "";
            return new SimpleStringProperty(narr + payee);
        });
        narrationCol.setPrefWidth(340);

        TableColumn<TransactionEntity, String> catCol = new TableColumn<>("CATEGORY");
        catCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory() != null ? c.getValue().getCategory() : "GENERAL"));
        catCol.setPrefWidth(140);

        TableColumn<TransactionEntity, String> modeCol = new TableColumn<>("MODE");
        modeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentMode() != null ? c.getValue().getPaymentMode() : "UPI"));
        modeCol.setPrefWidth(80);

        TableColumn<TransactionEntity, String> typeCol = new TableColumn<>("TYPE");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTxnType() != null ? c.getValue().getTxnType() : "DEBIT"));
        typeCol.setPrefWidth(90);
        typeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().addAll("badge", "CREDIT".equalsIgnoreCase(item) ? "badge-emerald" : "badge-rose");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TableColumn<TransactionEntity, String> amountCol = new TableColumn<>("AMOUNT");
        amountCol.setCellValueFactory(c -> new SimpleStringProperty(formatMoney(c.getValue().getAmount())));
        amountCol.setPrefWidth(120);
        amountCol.setStyle("-fx-alignment: CENTER_RIGHT;");

        TableColumn<TransactionEntity, String> balCol = new TableColumn<>("BALANCE AFTER");
        balCol.setCellValueFactory(c -> new SimpleStringProperty(formatMoney(c.getValue().getBalanceAfterTxn())));
        balCol.setPrefWidth(130);
        balCol.setStyle("-fx-alignment: CENTER_RIGHT;");

        table.getColumns().addAll(dateCol, narrationCol, catCol, modeCol, typeCol, amountCol, balCol);

        ObservableList<TransactionEntity> data = FXCollections.observableArrayList(allTxns);
        table.setItems(data);

        // Filter Logic
        Runnable filterAction = () -> {
            String query = searchField.getText().toLowerCase().trim();
            String selType = typeFilter.getValue();

            List<TransactionEntity> filtered = allTxns.stream().filter(t -> {
                boolean matchQuery = query.isEmpty() ||
                        (t.getNarration() != null && t.getNarration().toLowerCase().contains(query)) ||
                        (t.getMerchantPayee() != null && t.getMerchantPayee().toLowerCase().contains(query)) ||
                        (t.getCategory() != null && t.getCategory().toLowerCase().contains(query));

                boolean matchType = "All Types".equals(selType) ||
                        (t.getTxnType() != null && t.getTxnType().equalsIgnoreCase(selType));

                return matchQuery && matchType;
            }).toList();

            table.setItems(FXCollections.observableArrayList(filtered));
            countLabel.setText(filtered.size() + " of " + allTxns.size() + " Transactions");
        };

        searchField.textProperty().addListener((obs, o, n) -> filterAction.run());
        typeFilter.valueProperty().addListener((obs, o, n) -> filterAction.run());

        root.getChildren().addAll(toolbar, table);
        return root;
    }

    // =========================================================================
    // VIEW 3: FEATURE 1 — AUTO-RENEWAL EARLY WARNINGS
    // =========================================================================

    private Node buildAutoRenewalsView() {
        VBox root = new VBox(20);
        root.setFillWidth(true);

        // Fetch distinct auto-renewals from FintechToolsController
        var responseEntity = fintechToolsController.getAutoRenewals(30);
        Map<String, Object> data = responseEntity.getBody();
        if (data == null) data = Collections.emptyMap();

        int dueWithinAWeekCount = (int) data.getOrDefault("dueWithinAWeekCount", 0);
        BigDecimal dueWithinAWeekTotal = (BigDecimal) data.getOrDefault("dueWithinAWeekTotalAmount", BigDecimal.ZERO);
        BigDecimal totalCommitment = (BigDecimal) data.getOrDefault("totalLookaheadCommitment", BigDecimal.ZERO);
        int totalTracked = (int) data.getOrDefault("totalTrackedRenewals", 0);
        String fundingAcc = (String) data.getOrDefault("fundingAccountNumber", "SB-1002938172");
        BigDecimal currentBal = (BigDecimal) data.getOrDefault("currentBalance", BigDecimal.ZERO);
        boolean balanceSufficient = (boolean) data.getOrDefault("balanceSufficientForWeek", true);
        String headline = (String) data.getOrDefault("headline", "");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> renewals = (List<Map<String, Object>>) data.getOrDefault("renewals", Collections.emptyList());

        // 1. High-Visibility Warning Banner (if due within 7 days)
        VBox banner = new VBox(6);
        if (dueWithinAWeekCount > 0) {
            banner.getStyleClass().add(balanceSufficient ? "banner-warning" : "banner-critical");
            Label bannerTitle = new Label(headline);
            bannerTitle.getStyleClass().add(balanceSufficient ? "banner-warning-text" : "banner-critical-text");
            bannerTitle.setWrapText(true);

            Label bannerAdvice = new Label(
                    "💡 BANKING ADVISORY: NACH/e-Mandate auto-debit regulations require funds to be maintained in advance. If auto-debit bounces, banks levy ₹250-₹500 penalty charges and CIBIL credit scores are adversely impacted.");
            bannerAdvice.setStyle("-fx-font-size: 11px; -fx-text-fill: #e2e8f0;");
            bannerAdvice.setWrapText(true);

            banner.getChildren().addAll(bannerTitle, bannerAdvice);
        } else {
            banner.getStyleClass().add("banner-secure");
            Label bannerTitle = new Label("✅ All Clear: No auto-renewals scheduled within the next 7 days.");
            bannerTitle.getStyleClass().add("banner-secure-text");
            banner.getChildren().add(bannerTitle);
        }

        // 2. Metrics KPI Row
        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(16);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(25);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(25);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(25);
        ColumnConstraints c4 = new ColumnConstraints(); c4.setPercentWidth(25);
        metricsGrid.getColumnConstraints().addAll(c1, c2, c3, c4);

        VBox cardDueWeek = createMetricCard(
                "DUE WITHIN 7 DAYS",
                dueWithinAWeekCount + " Subscriptions",
                "Total ₹ required: " + formatMoney(dueWithinAWeekTotal),
                dueWithinAWeekCount > 0 ? "badge-amber" : "badge-emerald"
        );
        VBox cardBalance = createMetricCard(
                "FUNDING ACCOUNT (" + fundingAcc + ")",
                formatMoney(currentBal),
                balanceSufficient ? "Sufficient to cover next 7 days" : "DEFICIT DETECTED",
                balanceSufficient ? "badge-emerald" : "badge-rose"
        );
        VBox cardMonthly = createMetricCard(
                "TOTAL MONTHLY COMMITMENT",
                formatMoney(totalCommitment),
                "Across " + totalTracked + " tracked recurring services",
                "badge-blue"
        );
        VBox cardSafety = createMetricCard(
                "7-DAY BUFFER RATIO",
                (dueWithinAWeekTotal.compareTo(BigDecimal.ZERO) > 0 ? currentBal.divide(dueWithinAWeekTotal, 1, RoundingMode.HALF_UP) + "x" : "∞"),
                "Liquidity multiple over 7-day commitments",
                "badge-purple"
        );

        metricsGrid.add(cardDueWeek, 0, 0);
        metricsGrid.add(cardBalance, 1, 0);
        metricsGrid.add(cardMonthly, 2, 0);
        metricsGrid.add(cardSafety, 3, 0);

        // 3. Filter Controls
        HBox filterBox = new HBox(12);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label listTitle = new Label("SCHEDULED RECURRING SERVICES (" + totalTracked + ")");
        listTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-letter-spacing: 1px;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        ToggleGroup filterGroup = new ToggleGroup();
        ToggleButton tbAll = new ToggleButton("All (" + totalTracked + ")");
        ToggleButton tbWeek = new ToggleButton("⚠️ Due Within 7 Days (" + dueWithinAWeekCount + ")");
        ToggleButton tbLater = new ToggleButton("Upcoming Later (" + (totalTracked - dueWithinAWeekCount) + ")");

        tbAll.setToggleGroup(filterGroup);
        tbWeek.setToggleGroup(filterGroup);
        tbLater.setToggleGroup(filterGroup);
        tbAll.setSelected(true);

        tbAll.getStyleClass().add("button-secondary");
        tbWeek.getStyleClass().add("button-secondary");
        tbLater.getStyleClass().add("button-secondary");

        filterBox.getChildren().addAll(listTitle, sp, tbAll, tbWeek, tbLater);

        // 4. Renewal Items Container
        VBox itemsContainer = new VBox(12);
        Runnable populateList = () -> {
            itemsContainer.getChildren().clear();
            boolean showOnlyWeek = tbWeek.isSelected();
            boolean showOnlyLater = tbLater.isSelected();

            for (Map<String, Object> item : renewals) {
                boolean dueWithinWeek = (boolean) item.get("dueWithinAWeek");
                if (showOnlyWeek && !dueWithinWeek) continue;
                if (showOnlyLater && dueWithinWeek) continue;

                itemsContainer.getChildren().add(buildRenewalCard(item));
            }
        };

        tbAll.setOnAction(e -> populateList.run());
        tbWeek.setOnAction(e -> populateList.run());
        tbLater.setOnAction(e -> populateList.run());

        populateList.run();

        root.getChildren().addAll(banner, metricsGrid, filterBox, itemsContainer);
        return root;
    }

    private VBox buildRenewalCard(Map<String, Object> item) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");

        boolean dueWithinWeek = (boolean) item.get("dueWithinAWeek");
        int daysRemaining = (int) item.get("daysRemaining");
        BigDecimal amount = (BigDecimal) item.get("amount");
        String serviceName = (String) item.get("serviceName");
        String category = (String) item.get("category");
        String dueDate = (String) item.get("dueDate");
        String warningMsg = (String) item.get("warningMessage");
        String advisoryNote = (String) item.get("advisoryNote");
        String fundingAcc = (String) item.get("fundingAccountNumber");

        if (dueWithinWeek) {
            card.setStyle("-fx-border-color: #d97706; -fx-border-width: 1.5px; -fx-background-color: #171d2b;");
        }

        // Header Row
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(serviceName);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #f8fafc;");

        Label catBadge = new Label(category);
        catBadge.getStyleClass().addAll("badge", "badge-slate");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label amountLabel = new Label(formatMoney(amount));
        amountLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #f8fafc;");

        Label statusBadge;
        if (dueWithinWeek) {
            statusBadge = new Label("⚠️ DUE IN " + daysRemaining + " DAY" + (daysRemaining == 1 ? "" : "S") + " (< 1 WEEK)");
            statusBadge.getStyleClass().addAll("badge", "badge-amber");
        } else {
            statusBadge = new Label("SCHEDULED (IN " + daysRemaining + " DAYS)");
            statusBadge.getStyleClass().addAll("badge", "badge-blue");
        }

        header.getChildren().addAll(nameLabel, catBadge, sp, statusBadge, amountLabel);

        // Warning Text Box (Prominent notice to maintain balance)
        VBox alertBox = new VBox(4);
        alertBox.setStyle("-fx-background-color: " + (dueWithinWeek ? "#231805;" : "#0d1422;") +
                " -fx-padding: 8px 12px; -fx-background-radius: 4px;");

        Label warningLbl = new Label(warningMsg);
        warningLbl.setStyle("-fx-font-weight: 600; -fx-font-size: 12px; -fx-text-fill: " + (dueWithinWeek ? "#fde68a;" : "#94a3b8;"));
        warningLbl.setWrapText(true);

        Label noteLbl = new Label("Account: " + fundingAcc + "  •  Due Date: " + dueDate + "  •  " + advisoryNote);
        noteLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        noteLbl.setWrapText(true);

        alertBox.getChildren().addAll(warningLbl, noteLbl);

        card.getChildren().addAll(header, alertBox);
        return card;
    }

    // =========================================================================
    // VIEW 4: FEATURE 2 — MINIMUM BALANCE SHIELD
    // =========================================================================

    private Node buildMinBalanceShieldView() {
        VBox root = new VBox(20);
        root.setFillWidth(true);

        // Interactive simulation input bar
        HBox controlBar = new HBox(16);
        controlBar.setAlignment(Pos.CENTER_LEFT);
        controlBar.setStyle("-fx-background-color: #111724; -fx-padding: 12px 16px; -fx-background-radius: 6px; -fx-border-color: #1f2b40; -fx-border-width: 1px;");

        Label thresholdLabel = new Label("Bank Minimum Threshold:");
        thresholdLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #94a3b8;");

        ComboBox<BigDecimal> thresholdCombo = new ComboBox<>();
        thresholdCombo.getItems().addAll(
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(2500),
                BigDecimal.valueOf(5000)
        );
        thresholdCombo.setValue(BigDecimal.valueOf(1000));

        Label simLabel = new Label("Test Balance Scenario (Simulated):");
        simLabel.setStyle("-fx-font-weight: 700; -fx-text-fill: #94a3b8;");

        TextField simBalanceField = new TextField();
        simBalanceField.setPromptText("Leave blank for real balance");
        simBalanceField.setPrefWidth(200);

        Button applyBtn = new Button("🛡️ Recalculate Shield");
        applyBtn.getStyleClass().add("button-primary");

        Button resetBtn = new Button("Reset to Real");
        resetBtn.getStyleClass().add("button-secondary");

        controlBar.getChildren().addAll(thresholdLabel, thresholdCombo, simLabel, simBalanceField, applyBtn, resetBtn);

        // Container that re-renders dynamically when simulation changes
        VBox shieldDisplayContainer = new VBox(20);
        shieldDisplayContainer.setFillWidth(true);

        Runnable renderShield = () -> {
            shieldDisplayContainer.getChildren().clear();

            BigDecimal threshold = thresholdCombo.getValue();
            BigDecimal simBal = null;
            String txt = simBalanceField.getText().trim();
            if (!txt.isEmpty()) {
                try {
                    simBal = new BigDecimal(txt);
                } catch (NumberFormatException ignored) {}
            }

            var res = fintechToolsController.getMinBalanceShield(threshold, simBal);
            Map<String, Object> d = res.getBody();
            if (d == null) return;

            String shieldStatus = (String) d.get("shieldStatus");
            String headline = (String) d.get("statusHeadline");
            String accNum = (String) d.get("accountNumber");
            String bankName = (String) d.get("bankName");
            BigDecimal curBal = (BigDecimal) d.get("currentBalance");
            boolean isSim = (boolean) d.get("isSimulatedBalance");
            BigDecimal debits7 = (BigDecimal) d.get("upcomingDebitsNext7Days");
            BigDecimal debits30 = (BigDecimal) d.get("upcomingDebitsNext30Days");
            BigDecimal proj7 = (BigDecimal) d.get("projectedBalanceAfter7Days");
            BigDecimal buffer7 = (BigDecimal) d.get("safetyBuffer7Days");
            boolean breach = (boolean) d.get("breachRiskDetected");
            BigDecimal topUp = (BigDecimal) d.get("recommendedTopUp");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> threats = (List<Map<String, Object>>) d.get("threatTimeline");

            // 1. Status Alert Banner
            VBox banner = new VBox(6);
            if ("SHIELD_SECURE".equals(shieldStatus)) {
                banner.getStyleClass().add("banner-secure");
                Label bTitle = new Label(headline);
                bTitle.getStyleClass().add("banner-secure-text");
                bTitle.setWrapText(true);
                Label bSub = new Label("Bank Account: " + accNum + " (" + bankName + ")  •  Minimum required threshold: " + formatMoney(threshold) + " is fully safeguarded.");
                bSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #a7f3d0;");
                banner.getChildren().addAll(bTitle, bSub);
            } else {
                banner.getStyleClass().add("banner-critical");
                Label bTitle = new Label(headline);
                bTitle.getStyleClass().add("banner-critical-text");
                bTitle.setWrapText(true);
                Label bAction = new Label("⚡ IMMEDIATE ACTION: Deposit " + formatMoney(topUp) + " into " + accNum + " before debits occur to maintain Minimum Average Balance (MAB) compliance.");
                bAction.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #fecdd3;");
                banner.getChildren().addAll(bTitle, bAction);
            }

            // 2. Metrics Grid
            GridPane grid = new GridPane();
            grid.setHgap(16);
            grid.setVgap(16);
            ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(25);
            ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(25);
            ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(25);
            ColumnConstraints c4 = new ColumnConstraints(); c4.setPercentWidth(25);
            grid.getColumnConstraints().addAll(c1, c2, c3, c4);

            VBox cCur = createMetricCard(
                    "CURRENT BALANCE" + (isSim ? " (SIMULATED)" : ""),
                    formatMoney(curBal),
                    "Primary account: " + accNum,
                    isSim ? "badge-purple" : "badge-emerald"
            );
            VBox cProj = createMetricCard(
                    "PROJECTED 7-DAY BALANCE",
                    formatMoney(proj7),
                    "After ₹" + debits7 + " scheduled debits",
                    proj7.compareTo(threshold) >= 0 ? "badge-emerald" : "badge-rose"
            );
            VBox cBuffer = createMetricCard(
                    "SAFETY BUFFER OVER THRESHOLD",
                    formatMoney(buffer7),
                    "Target Minimum Balance: " + formatMoney(threshold),
                    buffer7.compareTo(BigDecimal.ZERO) >= 0 ? "badge-emerald" : "badge-amber"
            );
            VBox cTopUp = createMetricCard(
                    "REQUIRED TOP-UP DEPOSIT",
                    formatMoney(topUp),
                    topUp.compareTo(BigDecimal.ZERO) == 0 ? "No deposit required (Safe)" : "Deposit needed to restore shield",
                    topUp.compareTo(BigDecimal.ZERO) == 0 ? "badge-emerald" : "badge-rose"
            );

            grid.add(cCur, 0, 0);
            grid.add(cProj, 1, 0);
            grid.add(cBuffer, 2, 0);
            grid.add(cTopUp, 3, 0);

            // 3. Impending Debits Threat Ledger Table
            VBox threatsSection = new VBox(10);
            Label threatTitle = new Label("CHRONOLOGICAL DEDUCTION TRAJECTORY & THRESHOLD IMPACT");
            threatTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-letter-spacing: 1px;");

            TableView<Map<String, Object>> threatTable = new TableView<>();
            threatTable.setPrefHeight(260);
            threatTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            TableColumn<Map<String, Object>, String> colService = new TableColumn<>("SCHEDULED SERVICE");
            colService.setCellValueFactory(c -> new SimpleStringProperty((String) c.getValue().get("serviceName")));

            TableColumn<Map<String, Object>, String> colDue = new TableColumn<>("DUE DATE");
            colDue.setCellValueFactory(c -> new SimpleStringProperty((String) c.getValue().get("dueDate") + " (" + c.getValue().get("daysRemaining") + "d)"));

            TableColumn<Map<String, Object>, String> colAmt = new TableColumn<>("DEBIT AMOUNT");
            colAmt.setCellValueFactory(c -> new SimpleStringProperty(formatMoney((BigDecimal) c.getValue().get("amount"))));
            colAmt.setStyle("-fx-alignment: CENTER_RIGHT;");

            TableColumn<Map<String, Object>, String> colProjAfter = new TableColumn<>("PROJECTED BALANCE AFTER");
            colProjAfter.setCellValueFactory(c -> new SimpleStringProperty(formatMoney((BigDecimal) c.getValue().get("projectedBalanceAfter"))));
            colProjAfter.setStyle("-fx-alignment: CENTER_RIGHT;");

            TableColumn<Map<String, Object>, String> colBreach = new TableColumn<>("SHIELD STATUS");
            colBreach.setCellValueFactory(c -> {
                boolean b = (boolean) c.getValue().get("breachesMinBalance");
                return new SimpleStringProperty(b ? "BREACH RISK" : "SAFE");
            });
            colBreach.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Label b = new Label(item);
                        b.getStyleClass().addAll("badge", "SAFE".equals(item) ? "badge-emerald" : "badge-rose");
                        setGraphic(b);
                        setText(null);
                    }
                }
            });

            threatTable.getColumns().addAll(colService, colDue, colAmt, colProjAfter, colBreach);
            threatTable.setItems(FXCollections.observableArrayList(threats));

            threatsSection.getChildren().addAll(threatTitle, threatTable);

            shieldDisplayContainer.getChildren().addAll(banner, grid, threatsSection);
        };

        applyBtn.setOnAction(e -> renderShield.run());
        resetBtn.setOnAction(e -> {
            simBalanceField.clear();
            renderShield.run();
        });

        renderShield.run();

        root.getChildren().addAll(controlBar, shieldDisplayContainer);
        return root;
    }

    // =========================================================================
    // VIEW 5: SCHOLARSHIP RUNWAY & CTC SANDBOX
    // =========================================================================

    private Node buildStudentToolsView() {
        VBox root = new VBox(24);
        root.setFillWidth(true);

        // Section 1: Scholarship Runway Calculator
        VBox runwayCard = new VBox(14);
        runwayCard.getStyleClass().add("card");

        Label rTitle = new Label("SCHOLARSHIP & LIQUIDITY RUNWAY CALCULATOR");
        rTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-letter-spacing: 1px;");

        HBox runwayControls = new HBox(16);
        runwayControls.setAlignment(Pos.CENTER_LEFT);

        Label burnLabel = new Label("Monthly Burn Rate (₹):");
        burnLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #cbd5e1;");

        BigDecimal totalDebits = transactionRepository != null ? transactionRepository.getTotalDebits() : null;
        BigDecimal initialBurn = (totalDebits != null && totalDebits.compareTo(BigDecimal.ZERO) > 0)
                ? totalDebits.divide(BigDecimal.valueOf(6), 0, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(15000);
        TextField burnField = new TextField(initialBurn.toPlainString());
        burnField.setPrefWidth(120);

        CheckBox fdToggle = new CheckBox("Include Fixed Deposit as Emergency Reserve");
        fdToggle.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: 600;");

        Button calcRunwayBtn = new Button("Calculate Runway");
        calcRunwayBtn.getStyleClass().add("button-primary");

        runwayControls.getChildren().addAll(burnLabel, burnField, fdToggle, calcRunwayBtn);

        HBox runwayResultBox = new HBox(20);
        runwayResultBox.setAlignment(Pos.CENTER_LEFT);
        runwayResultBox.setStyle("-fx-background-color: #0d1422; -fx-padding: 12px 16px; -fx-background-radius: 6px;");

        Label runwayMetric = new Label("-- Months");
        runwayMetric.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: #34d399;");

        VBox runwayExplanation = new VBox(2);
        Label rExpl = new Label("CALCULATING RUNWAY...");
        rExpl.setStyle("-fx-font-weight: 700; -fx-font-size: 10px; -fx-text-fill: #10b981;");
        Label rSub = new Label("Based on your liquid capital divided by average monthly student expenditure.");
        rSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        runwayExplanation.getChildren().addAll(rExpl, rSub);

        runwayResultBox.getChildren().addAll(runwayMetric, runwayExplanation);

        calcRunwayBtn.setOnAction(e -> {
            try {
                BigDecimal burn = new BigDecimal(burnField.getText().trim());
                boolean incFd = fdToggle.isSelected();
                var res = fintechToolsController.calculateRunway(burn, incFd);
                Map<String, Object> body = res.getBody();
                if (body != null) {
                    BigDecimal months = (BigDecimal) body.get("runwayMonths");
                    String status = (String) body.get("safetyStatus");
                    runwayMetric.setText(months + " Months");
                    rExpl.setText(status + " CAPITAL DURATION");
                    if ("COMFORTABLE".equals(status)) {
                        runwayMetric.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: #34d399;");
                    } else {
                        runwayMetric.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: #fbbf24;");
                    }
                }
            } catch (Exception ex) {
                runwayMetric.setText("Error");
            }
        });

        // Trigger dynamic calculation immediately from database
        calcRunwayBtn.fire();

        runwayCard.getChildren().addAll(rTitle, runwayControls, runwayResultBox);

        // Section 2: Post-Graduation Salary CTC Sandbox
        VBox ctcCard = new VBox(14);
        ctcCard.getStyleClass().add("card");

        Label ctcTitle = new Label("POST-GRADUATION SALARY SANDBOX (NEW TAX REGIME FY 2024-25)");
        ctcTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-letter-spacing: 1px;");

        HBox ctcControls = new HBox(16);
        ctcControls.setAlignment(Pos.CENTER_LEFT);

        Label ctcLabel = new Label("Annual CTC (₹):");
        ctcLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #cbd5e1;");

        TextField ctcField = new TextField("1200000");
        ctcField.setPrefWidth(140);

        Label cityLabel = new Label("Placement City:");
        cityLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #cbd5e1;");

        ComboBox<String> cityCombo = new ComboBox<>();
        cityCombo.getItems().addAll("Bangalore", "Mumbai", "Delhi", "Hyderabad", "Pune");
        cityCombo.setValue("Bangalore");

        Button calcCtcBtn = new Button("Simulate In-Hand");
        calcCtcBtn.getStyleClass().add("button-primary");

        ctcControls.getChildren().addAll(ctcLabel, ctcField, cityLabel, cityCombo, calcCtcBtn);

        GridPane ctcGrid = new GridPane();
        ctcGrid.setHgap(16);
        ctcGrid.setVgap(12);

        calcCtcBtn.setOnAction(e -> {
            try {
                BigDecimal ctc = new BigDecimal(ctcField.getText().trim());
                String city = cityCombo.getValue();
                var res = fintechToolsController.calculateSalarySandbox(ctc, city);
                Map<String, Object> body = res.getBody();
                if (body != null) {
                    ctcGrid.getChildren().clear();
                    addAccountDetail(ctcGrid, 0, 0, "Monthly Gross", formatMoney((BigDecimal) body.get("monthlyGross")));
                    addAccountDetail(ctcGrid, 1, 0, "Monthly Net In-Hand", formatMoney((BigDecimal) body.get("monthlyNetInHand")));
                    addAccountDetail(ctcGrid, 2, 0, "Annual Tax", formatMoney((BigDecimal) body.get("annualIncomeTax")));
                    addAccountDetail(ctcGrid, 3, 0, "City Living Cost", formatMoney(BigDecimal.valueOf((int) body.get("estimatedCityMonthlyLivingCost"))));
                    addAccountDetail(ctcGrid, 4, 0, "Projected Monthly Savings", formatMoney((BigDecimal) body.get("projectedMonthlySavings")));
                }
            } catch (Exception ignored) {}
        });

        calcCtcBtn.fire();

        ctcCard.getChildren().addAll(ctcTitle, ctcControls, ctcGrid);

        root.getChildren().addAll(runwayCard, ctcCard);
        return root;
    }

    // =========================================================================
    // VIEW 6: AA GATEWAY & SIMULATOR
    // =========================================================================

    private Node buildSimulatorView() {
        VBox root = new VBox(20);
        root.setFillWidth(true);

        // AA Gateway Card
        VBox aaCard = new VBox(12);
        aaCard.getStyleClass().add("card");

        Label aaTitle = new Label("RBI ACCOUNT AGGREGATOR (SETU AA SANDBOX GATEWAY)");
        aaTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-letter-spacing: 1px;");

        Label aaDesc = new Label("Ledgr functions as a compliant Financial Information User (FIU). Performing a sync executes the 5-phase cryptographic handshake: Consent Negotiation, OTP Verification, FIP Banking Key-Exchange, Statement Fetch, and Ledger Ingestion.");
        aaDesc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        aaDesc.setWrapText(true);

        HBox aaActions = new HBox(12);
        aaActions.setAlignment(Pos.CENTER_LEFT);

        Button fullSyncBtn = new Button("🔄 Execute Multi-Phase AA Sync");
        fullSyncBtn.getStyleClass().add("button-primary");

        Label syncStatus = new Label("Ready");
        syncStatus.setStyle("-fx-text-fill: #64748b; -fx-font-weight: 600;");

        aaActions.getChildren().addAll(fullSyncBtn, syncStatus);

        fullSyncBtn.setOnAction(e -> {
            fullSyncBtn.setDisable(true);
            syncStatus.setText("Executing AA 5-phase handshake...");
            new Thread(() -> {
                try {
                    var res = fiuIngestionService.executeFullSync("student@setuaa");
                    Platform.runLater(() -> {
                        fullSyncBtn.setDisable(false);
                        syncStatus.setText("✅ Sync Completed: " + res.get("accountsSynchronized") + " accounts & " + res.get("transactionsSynchronized") + " transactions synced.");
                        navigateTo("ACCOUNTS");
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        fullSyncBtn.setDisable(false);
                        syncStatus.setText("❌ Sync Error: " + ex.getMessage());
                    });
                }
            }).start();
        });

        aaCard.getChildren().addAll(aaTitle, aaDesc, aaActions);

        // Live Simulator Card
        VBox simCard = new VBox(12);
        simCard.getStyleClass().add("card");

        Label simTitle = new Label("REALTIME TRANSACTION SIMULATOR");
        simTitle.setStyle("-fx-font-weight: 800; -fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-letter-spacing: 1px;");

        Label simDesc = new Label("The simulator generates realistic student UPI, ATM, Canteen, and Subscriptions transactions with random delays (1.2s - 3.2s) in the background. You can also inject a manual transaction instantly.");
        simDesc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        simDesc.setWrapText(true);

        HBox simActions = new HBox(12);
        simActions.setAlignment(Pos.CENTER_LEFT);

        Button toggleStreamBtn = new Button(simulatorService.isStreamingActive() ? "⏸️ Pause Simulator Stream" : "▶️ Resume Simulator Stream");
        toggleStreamBtn.getStyleClass().add("button-secondary");

        Button manualTxnBtn = new Button("⚡ Inject 1 Mock Transaction");
        manualTxnBtn.getStyleClass().add("button-primary");

        simActions.getChildren().addAll(toggleStreamBtn, manualTxnBtn);

        toggleStreamBtn.setOnAction(e -> {
            if (simulatorService.isStreamingActive()) {
                simulatorService.stopLiveStreaming();
                toggleStreamBtn.setText("▶️ Resume Simulator Stream");
                simulatorStatusPill.setText("● PAUSED");
                simulatorStatusPill.getStyleClass().removeAll("badge-emerald");
                simulatorStatusPill.getStyleClass().add("badge-amber");
            } else {
                simulatorService.startLiveStreamingWithRandomDelay(1200, 3200);
                toggleStreamBtn.setText("⏸️ Pause Simulator Stream");
                simulatorStatusPill.setText("● STREAMING");
                simulatorStatusPill.getStyleClass().removeAll("badge-amber");
                simulatorStatusPill.getStyleClass().add("badge-emerald");
            }
        });

        manualTxnBtn.setOnAction(e -> {
            simulatorService.generateAndPersistTransaction(null);
            navigateTo("TRANSACTIONS");
        });

        simCard.getChildren().addAll(simTitle, simDesc, simActions);

        root.getChildren().addAll(aaCard, simCard);
        return root;
    }

    private void handleSyncAAGateway() {
        new Thread(() -> {
            try {
                fiuIngestionService.executeFullSync("student@setuaa");
                Platform.runLater(() -> navigateTo(currentView.get()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
}
