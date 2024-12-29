import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Vector;
import javax.swing.table.DefaultTableCellRenderer;
import java.util.HashSet;
import java.util.Set;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HastaneOtomasyonu extends JFrame {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/hastane_db";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "123";
    private JTable yonetimTablosu;
    private Connection conn;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private String currentUserRole;
    private String currentUsername;

    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);    // Ana mavi
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);  // Açık mavi
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241); // Açık gri
    private static final Color PANEL_COLOR = Color.WHITE;
    private static final Color TEXT_COLOR = new Color(44, 62, 80);         // Koyu lacivert
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);    // Yeşil
    private static final Color ERROR_COLOR = new Color(231, 76, 60);       // Kırmızı
    private static final Color HOVER_COLOR = new Color(52, 152, 219);      // Hover mavi

    // Font sabitleri
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    public HastaneOtomasyonu() {
        setTitle("Hastane Randevu Sistemi");
        setSize(1000, 700); // Pencere boyutunu büyüttüm
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false); // Boyut değiştirmeyi engelle

        // Genel tema ayarları
        try {
            UIManager.put("Panel.background", PANEL_COLOR);
            UIManager.put("OptionPane.background", PANEL_COLOR);
            UIManager.put("Button.font", BUTTON_FONT);
            UIManager.put("Label.font", LABEL_FONT);
            UIManager.put("TextField.font", LABEL_FONT);
            UIManager.put("ComboBox.font", LABEL_FONT);
            UIManager.put("Table.font", LABEL_FONT);
            UIManager.put("Table.gridColor", new Color(230, 230, 230));
            UIManager.put("Table.selectionBackground", SECONDARY_COLOR);
            UIManager.put("Table.selectionForeground", Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Veritabanı bağlantısı
        baglantiKur();

        // Ana panel ayarları
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BACKGROUND_COLOR);

        // İçerik paneli ayarları
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Alt panelleri oluştur
        olusturKarsilamaEkrani();
        olusturGirisEkrani();
        olusturHastaRandevuEkrani();
        olusturYonetimPaneli();

        // Ana paneli ekle
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        add(contentPanel);

        // Pencere ikonunu ayarla (opsiyonel)
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icons/hospital_icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // İkon bulunamazsa sessizce devam et
        }
    }

    private void baglantiKur() {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Veritabanı bağlantı hatası: " + e.getMessage());
            System.exit(1);
        }
    }
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        // Hover efekti
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
            }
        });

        return button;
    }
    private void olusturKarsilamaEkrani() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Başlık
        JLabel baslik = new JLabel("Hastane Yönetim Sistemi", SwingConstants.CENTER);
        baslik.setFont(TITLE_FONT);
        baslik.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(baslik, gbc);

        // Butonlar için panel
        JPanel butonPanel = new JPanel(new GridLayout(3, 1, 0, 15));
        butonPanel.setBackground(BACKGROUND_COLOR);

        // Butonları oluştur ve stilleri uygula
        JButton btnAdminGiris = createStyledButton("Admin Girişi");
        JButton btnDoktorGiris = createStyledButton("Doktor Girişi");
        JButton btnHastaIslemleri = createStyledButton("Hasta İşlemleri");

        // Buton boyutlarını ayarla
        Dimension buttonSize = new Dimension(250, 45);
        btnAdminGiris.setPreferredSize(buttonSize);
        btnDoktorGiris.setPreferredSize(buttonSize);
        btnHastaIslemleri.setPreferredSize(buttonSize);

        butonPanel.add(btnAdminGiris);
        butonPanel.add(btnDoktorGiris);
        butonPanel.add(btnHastaIslemleri);

        gbc.gridy = 1;
        gbc.insets = new Insets(30, 15, 15, 15);
        panel.add(butonPanel, gbc);

        // Event handlers
        btnAdminGiris.addActionListener(e -> girisDialogGoster("ADMIN"));
        btnDoktorGiris.addActionListener(e -> girisDialogGoster("DOKTOR"));
        btnHastaIslemleri.addActionListener(e -> cardLayout.show(mainPanel, "randevu"));

        mainPanel.add(panel, "karsilama");
    }
    private void girisDialogGoster(String rol) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), "Giriş", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Başlık
        JLabel baslik = new JLabel(rol + " Girişi", SwingConstants.CENTER);
        baslik.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        dialog.add(baslik, gbc);

        // Kullanıcı adı
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1;
        dialog.add(new JLabel("Kullanıcı Adı:"), gbc);
        JTextField kullaniciAdi = new JTextField(15);
        gbc.gridx = 1;
        dialog.add(kullaniciAdi, gbc);

        // Şifre
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Şifre:"), gbc);
        JPasswordField sifre = new JPasswordField(15);
        gbc.gridx = 1;
        dialog.add(sifre, gbc);

        // Giriş butonu
        JButton girisBtn = new JButton("Giriş");
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        dialog.add(girisBtn, gbc);

        girisBtn.addActionListener(e -> {
            try {
                String username = kullaniciAdi.getText();
                String password = new String(sifre.getPassword());

                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT username, rol FROM kullanicilar " +
                                "WHERE username = ? AND password = ? AND rol = ?");
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, rol);

                ResultSet rs = pstmt.executeQuery();

                if(rs.next()) {
                    currentUsername = username;
                    currentUserRole = rol;

                    // Log giriş işlemini
                    PreparedStatement logStmt = conn.prepareStatement(
                            "SELECT log_kullanici_islem('GIRIS', ?)");
                    logStmt.setString(1, currentUsername);
                    logStmt.execute();

                    System.out.println("Giriş başarılı - Kullanıcı: " + currentUsername + ", Rol: " + currentUserRole);

                    dialog.dispose();
                    olusturYonetimPaneli(); // Önce paneli oluştur
                    cardLayout.show(mainPanel, "yonetim"); // Sonra göster
                } else {
                    JOptionPane.showMessageDialog(dialog,
                            "Geçersiz kullanıcı adı veya şifre!",
                            "Hata",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog,
                        "Giriş yapılırken hata oluştu: " + ex.getMessage(),
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(mainPanel);
        dialog.setVisible(true);
    }
    private void olusturGirisEkrani() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        JTextField txtUsername = new JTextField(20);
        JPasswordField txtPassword = new JPasswordField(20);
        JButton btnGiris = new JButton("Giriş");
        JButton btnGeri = new JButton("Geri");

        // tabloPanel ve yonetimTablosu buradan kaldırıldı

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Kullanıcı Adı:"), gbc);

        gbc.gridx = 1;
        panel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Şifre:"), gbc);

        gbc.gridx = 1;
        panel.add(txtPassword, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(btnGiris);
        buttonPanel.add(btnGeri);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        btnGiris.addActionListener(e -> {
            try {
                String username = txtUsername.getText();  // Kullanıcı adını al
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT rol FROM kullanicilar WHERE username = ? AND password = ?");
                pstmt.setString(1, username);
                pstmt.setString(2, new String(txtPassword.getPassword()));

                ResultSet rs = pstmt.executeQuery();
                if(rs.next()) {
                    currentUserRole = rs.getString("rol");
                    currentUsername = username;  // Kullanıcı adını sakla

                    // Giriş logunu kaydet
                    PreparedStatement logStmt = conn.prepareStatement(
                            "SELECT log_kullanici_islem('GIRIS', ?)");
                    logStmt.setString(1, username);
                    logStmt.execute();

                    cardLayout.show(mainPanel, "yonetim");
                } else {
                    JOptionPane.showMessageDialog(this, "Geçersiz kullanıcı adı veya şifre!");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Giriş hatası: " + ex.getMessage());
            }
        });

        btnGeri.addActionListener(e -> cardLayout.show(mainPanel, "karsilama"));

        mainPanel.add(panel, "giris");
    }   private void olusturHastaRandevuEkrani() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Üst panel - Randevu Al ve Randevu İptal seçenekleri
        JPanel ustPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnRandevuAl = new JButton("Randevu Al");
        JButton btnRandevuIptal = new JButton("Randevu İptal");
        JButton btnGeri = new JButton("Ana Menüye Dön"); // Yeni eklenen buton
        ustPanel.add(btnRandevuAl);
        ustPanel.add(btnRandevuIptal);
        ustPanel.add(btnGeri);

        // Ana panel için CardLayout kullan
        JPanel anaPanel = new JPanel(new CardLayout());
        CardLayout randevuCardLayout = (CardLayout) anaPanel.getLayout();

        // Randevu alma paneli
        JPanel randevuAlPanel = olusturRandevuAlmaFormu();

        // Randevu iptal paneli
        JPanel randevuIptalPanel = olusturRandevuIptalFormu();

        anaPanel.add(randevuAlPanel, "randevuAl");
        anaPanel.add(randevuIptalPanel, "randevuIptal");

        btnRandevuAl.addActionListener(e -> randevuCardLayout.show(anaPanel, "randevuAl"));
        btnRandevuIptal.addActionListener(e -> randevuCardLayout.show(anaPanel, "randevuIptal"));

        btnGeri.addActionListener(e -> cardLayout.show(mainPanel, "karsilama"));

        panel.add(ustPanel, BorderLayout.NORTH);
        panel.add(anaPanel, BorderLayout.CENTER);

        mainPanel.add(panel, "randevu");
    }
    private JPanel olusturRandevuAlmaFormu() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // TC Kimlik
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("TC Kimlik No:"), gbc);
        JTextField tcField = new JTextField(11);
        gbc.gridx = 1;
        panel.add(tcField, gbc);

        // Doktor seçimi
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Doktor:"), gbc);
        JComboBox<String> doktorCombo = new JComboBox<>();
        gbc.gridx = 1;
        panel.add(doktorCombo, gbc);

        // Doktorları yükle
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT id, ad, soyad, uzmanlik FROM doktorlar ORDER BY ad, soyad");
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                String doktorBilgi = rs.getInt("id") + "-" +
                        rs.getString("ad") + " " +
                        rs.getString("soyad") + " (" +
                        rs.getString("uzmanlik") + ")";
                doktorCombo.addItem(doktorBilgi);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Tarih seçimi
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Tarih:"), gbc);
        JComboBox<LocalDate> tarihCombo = new JComboBox<>();
        gbc.gridx = 1;
        panel.add(tarihCombo, gbc);

        // Gelecek 7 günü ekle (hafta sonları hariç)
        LocalDate tarih = LocalDate.now();
        for(int i = 0; i < 7; i++) {
            if(tarih.getDayOfWeek().getValue() < 6) { // Pazartesi=1, Cumartesi=6
                tarihCombo.addItem(tarih);
            }
            tarih = tarih.plusDays(1);
        }

        // Saat seçimi
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Saat:"), gbc);
        JComboBox<String> saatCombo = new JComboBox<>();
        gbc.gridx = 1;
        panel.add(saatCombo, gbc);

        // Doktor veya tarih seçildiğinde saatleri güncelle
        doktorCombo.addActionListener(e -> musaitSaatleriGuncelle(doktorCombo, tarihCombo, saatCombo));
        tarihCombo.addActionListener(e -> musaitSaatleriGuncelle(doktorCombo, tarihCombo, saatCombo));

        // Randevu Al butonu
        JButton btnRandevuAl = new JButton("Randevu Al");
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(btnRandevuAl, gbc);

        btnRandevuAl.addActionListener(e -> {
            try {
                if(tcField.getText().trim().length() != 11) {
                    JOptionPane.showMessageDialog(panel, "Geçerli bir TC kimlik numarası giriniz!");
                    return;
                }

                if(doktorCombo.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(panel, "Lütfen bir doktor seçiniz!");
                    return;
                }

                if(tarihCombo.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(panel, "Lütfen bir tarih seçiniz!");
                    return;
                }

                String secilenSaat = saatCombo.getSelectedItem().toString();
                if(secilenSaat.contains("(DOLU)")) {
                    JOptionPane.showMessageDialog(panel,
                            "Bu saat dolu! Lütfen başka bir saat seçiniz.",
                            "Randevu Dolu",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Doktor ID'sini string'den ayıkla
                String doktorBilgisi = doktorCombo.getSelectedItem().toString();
                int doktorId = Integer.parseInt(doktorBilgisi.split("-")[0]);

                // Randevu al
                randevuAl(
                        tcField.getText(),
                        doktorId,
                        (LocalDate)tarihCombo.getSelectedItem(),
                        secilenSaat.split(" ")[0] // "(DOLU)" yazısını kaldır
                );

                // Başarılı olursa alanları temizle
                tcField.setText("");
                doktorCombo.setSelectedIndex(0);
                tarihCombo.setSelectedIndex(0);
                saatCombo.setSelectedIndex(0);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel,
                        "Randevu oluşturulurken hata: " + ex.getMessage(),
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }
    private JPanel olusturRandevuIptalFormu() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // TC Kimlik alanı
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("TC Kimlik No:"), gbc);
        JTextField tcField = new JTextField(20); // Genişliği artırıldı
        gbc.gridx = 1;
        panel.add(tcField, gbc);

        // Randevuları göster butonu
        JButton btnRandevulariGoster = new JButton("Randevuları Göster");
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(btnRandevulariGoster, gbc);

        // Randevu tablosu
        JTable randevuTablo = new JTable();
        JScrollPane scrollPane = new JScrollPane(randevuTablo);
        scrollPane.setPreferredSize(new Dimension(800, 400)); // Tablo boyutu artırıldı
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollPane, gbc);

        // Tablo sütun genişliklerini ayarla
        randevuTablo.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        randevuTablo.setRowHeight(25); // Satır yüksekliği artırıldı

        // İptal butonu
        JButton btnIptal = new JButton("Seçili Randevuyu İptal Et");
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(btnIptal, gbc);

        // Panel boyutunu ayarla
        panel.setPreferredSize(new Dimension(850, 500));

        btnRandevulariGoster.addActionListener(e -> {
            if ("DOKTOR".equals(currentUserRole)) {
                // Doktor için direkt kendi randevularını göster
                randevulariGoster(null, randevuTablo);
            } else {
                // Hasta için TC kontrolü yap
                String tc = tcField.getText().trim();
                if(tc.length() != 11) {
                    JOptionPane.showMessageDialog(panel, "Geçerli bir TC kimlik numarası giriniz!");
                    return;
                }
                randevulariGoster(tc, randevuTablo);
            }
        });

        btnIptal.addActionListener(e -> {
            int selectedRow = randevuTablo.getSelectedRow();
            if(selectedRow == -1) {
                JOptionPane.showMessageDialog(panel, "Lütfen iptal edilecek randevuyu seçiniz!");
                return;
            }

            int randevuId = (int) randevuTablo.getValueAt(selectedRow, 0);
            if ("DOKTOR".equals(currentUserRole)) {
                randevuyuIptalEt(randevuId, null);
            } else {
                randevuyuIptalEt(randevuId, tcField.getText());
            }
            randevulariGoster("DOKTOR".equals(currentUserRole) ? null : tcField.getText(), randevuTablo);
        });

        // Doktor girişi yapıldıysa otomatik randevuları göster
        if ("DOKTOR".equals(currentUserRole)) {
            randevulariGoster(null, randevuTablo);
        }

        return panel;
    }

    private void randevulariGoster(String tc, JTable tablo) {
        try {
            String sql;
            PreparedStatement pstmt;

            if ("DOKTOR".equals(currentUserRole)) {
                // Doktor için sadece kendi randevularını göster
                sql = "SELECT r.id, h.tc_no, h.ad || ' ' || h.soyad as hasta_adi, " +
                        "r.tarih, r.saat, r.durum " +
                        "FROM randevular r " +
                        "JOIN hastalar h ON r.hasta_id = h.id " +
                        "JOIN doktorlar d ON r.doktor_id = d.id " +
                        "JOIN kullanicilar k ON d.kullanici_id = k.id " +
                        "WHERE k.username = ? AND r.tarih >= CURRENT_DATE " +
                        "ORDER BY r.tarih, r.saat";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, currentUsername);
            } else {
                // Hasta için normal sorgu
                sql = "SELECT r.id, d.ad || ' ' || d.soyad as doktor_adi, " +
                        "r.tarih, r.saat, r.durum " +
                        "FROM randevular r " +
                        "JOIN hastalar h ON r.hasta_id = h.id " +
                        "JOIN doktorlar d ON r.doktor_id = d.id " +
                        "WHERE h.tc_no = ? AND r.tarih >= CURRENT_DATE " +
                        "ORDER BY r.tarih, r.saat";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, tc);
            }

            ResultSet rs = pstmt.executeQuery();

            Vector<String> kolonlar = new Vector<>();
            kolonlar.add("ID");
            if ("DOKTOR".equals(currentUserRole)) {
                kolonlar.add("TC No");
                kolonlar.add("Hasta Adı");
            } else {
                kolonlar.add("Doktor");
            }
            kolonlar.add("Tarih");
            kolonlar.add("Saat");
            kolonlar.add("Durum");

            Vector<Vector<Object>> veriler = new Vector<>();
            while(rs.next()) {
                Vector<Object> satir = new Vector<>();
                satir.add(rs.getInt("id"));
                if ("DOKTOR".equals(currentUserRole)) {
                    satir.add(rs.getString("tc_no"));
                    satir.add(rs.getString("hasta_adi"));
                } else {
                    satir.add(rs.getString("doktor_adi"));
                }
                satir.add(rs.getDate("tarih"));
                satir.add(rs.getTime("saat"));
                satir.add(rs.getString("durum"));
                veriler.add(satir);
            }

            DefaultTableModel model = new DefaultTableModel(veriler, kolonlar) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            tablo.setModel(model);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Randevular getirilirken hata oluştu: " + e.getMessage());
        }
    }

    private void randevuyuIptalEt(int randevuId, String tc) {
        try {
            // Önce randevunun bu TC'ye ait olduğunu kontrol et
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT r.id FROM randevular r " +
                            "JOIN hastalar h ON r.hasta_id = h.id " +
                            "WHERE r.id = ? AND h.tc_no = ? AND r.durum = 'BEKLEMEDE'");
            checkStmt.setInt(1, randevuId);
            checkStmt.setString(2, tc);

            if(!checkStmt.executeQuery().next()) {
                JOptionPane.showMessageDialog(null, "Bu randevuyu iptal etme yetkiniz yok!");
                return;
            }

            // Randevuyu iptal et
            PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE randevular SET durum = 'IPTAL' WHERE id = ?");
            updateStmt.setInt(1, randevuId);

            int affectedRows = updateStmt.executeUpdate();

            if(affectedRows > 0) {
                JOptionPane.showMessageDialog(null, "Randevu başarıyla iptal edildi!");
            } else {
                JOptionPane.showMessageDialog(null, "Randevu iptal edilemedi!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Randevu iptal edilirken hata oluştu: " + e.getMessage());
        }
    }
    private void musaitSaatleriGuncelle(JComboBox<String> doktorCombo,
                                        JComboBox<LocalDate> tarihCombo,
                                        JComboBox<String> saatCombo) {
        try {
            saatCombo.removeAllItems();

            if (doktorCombo.getSelectedItem() == null || tarihCombo.getSelectedItem() == null) {
                return;
            }

            // Doktor ID'sini al
            String doktorBilgisi = doktorCombo.getSelectedItem().toString();
            int doktorId = Integer.parseInt(doktorBilgisi.split("-")[0]);

            // Seçili tarihteki dolu saatleri getir
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT saat FROM randevular " +
                            "WHERE doktor_id = ? AND tarih = ? AND durum = 'BEKLEMEDE'");
            pstmt.setInt(1, doktorId);
            pstmt.setDate(2, java.sql.Date.valueOf((LocalDate)tarihCombo.getSelectedItem()));
            ResultSet rs = pstmt.executeQuery();

            // Dolu saatleri Set'e ekle
            Set<String> doluSaatler = new HashSet<>();
            while(rs.next()) {
                doluSaatler.add(rs.getTime("saat").toString().substring(0, 5));
            }

            // Tüm saatleri ekle ve dolu olanları işaretle
            String[] saatler = {"09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                    "13:00", "13:30", "14:00", "14:30", "15:00", "15:30"};

            for (String saat : saatler) {
                if (doluSaatler.contains(saat)) {
                    saatCombo.addItem(saat + " (DOLU)");
                } else {
                    saatCombo.addItem(saat);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Müsait saatler güncellenirken hata oluştu!");
        }
    }


    private void randevuAl(String tc, int doktorId, LocalDate tarih, String saat) {
        try {
            conn.setAutoCommit(false);

            // Önce hastanın var olup olmadığını kontrol et
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT id FROM hastalar WHERE tc_no = ?");
            checkStmt.setString(1, tc);
            ResultSet rs = checkStmt.executeQuery();

            int hastaId;
            if (!rs.next()) {
                // Hasta yoksa, yeni hasta kaydı için bilgileri al
                String ad = JOptionPane.showInputDialog(null, "Hasta Adı:");
                if (ad == null || ad.trim().isEmpty()) {
                    throw new IllegalArgumentException("Hasta adı boş olamaz!");
                }

                String soyad = JOptionPane.showInputDialog(null, "Hasta Soyadı:");
                if (soyad == null || soyad.trim().isEmpty()) {
                    throw new IllegalArgumentException("Hasta soyadı boş olamaz!");
                }

                String telefon = JOptionPane.showInputDialog(null, "Telefon:");
                if (telefon == null || telefon.trim().isEmpty()) {
                    throw new IllegalArgumentException("Telefon numarası boş olamaz!");
                }

                // Yeni hasta kaydı oluştur
                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO hastalar (tc_no, ad, soyad, telefon) VALUES (?, ?, ?, ?) RETURNING id");
                insertStmt.setString(1, tc);
                insertStmt.setString(2, ad.trim());
                insertStmt.setString(3, soyad.trim());
                insertStmt.setString(4, telefon.trim());
                rs = insertStmt.executeQuery();
                rs.next();
                hastaId = rs.getInt(1);
            } else {
                hastaId = rs.getInt("id");
            }

            // Saat formatını düzelt
            if (!saat.contains(":")) {
                saat = saat + ":00";
            }
            if (saat.length() == 5) {
                saat = saat + ":00";
            }

            // Randevu oluştur
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO randevular (hasta_id, doktor_id, tarih, saat, durum) " +
                            "VALUES (?, ?, ?, ?, 'BEKLEMEDE')");
            pstmt.setInt(1, hastaId);
            pstmt.setInt(2, doktorId);
            pstmt.setDate(3, java.sql.Date.valueOf(tarih));
            pstmt.setTime(4, java.sql.Time.valueOf(saat));
            pstmt.executeUpdate();

            conn.commit();
            JOptionPane.showMessageDialog(null, "Randevu başarıyla oluşturuldu!");

        } catch (IllegalArgumentException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, e.getMessage());
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Randevu oluşturulurken hata: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void olusturYonetimPaneli() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if ("ADMIN".equals(currentUserRole)) {
            // Admin için tüm seçenekler
            JPanel ustPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            JComboBox<String> tabloSecim = new JComboBox<>(new String[]{
                    "Doktorlar", "Hastalar", "Randevular", "Sistem Logları"
            });

            JButton btnDoktorEkle = new JButton("Yeni Doktor Ekle");
            ustPanel.add(new JLabel("Tablo: "));
            ustPanel.add(tabloSecim);
            ustPanel.add(Box.createHorizontalStrut(20));
            ustPanel.add(btnDoktorEkle);

            // Tablo
            yonetimTablosu = new JTable();
            JScrollPane scrollPane = new JScrollPane(yonetimTablosu);

            btnDoktorEkle.addActionListener(e -> doktorEkleDialogGoster());
            tabloSecim.addActionListener(e -> tabloyuGuncelle(yonetimTablosu, (String) tabloSecim.getSelectedItem()));

            panel.add(ustPanel, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);

            // İlk tabloyu göster
            tabloyuGuncelle(yonetimTablosu, "Doktorlar");

        } else if ("DOKTOR".equals(currentUserRole)) {
            // Doktor için sadece kendi randevuları
            JPanel ustPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel baslik = new JLabel("Randevularım");
            baslik.setFont(new Font("Arial", Font.BOLD, 16));
            ustPanel.add(baslik);

            yonetimTablosu = new JTable();
            JScrollPane scrollPane = new JScrollPane(yonetimTablosu);

            panel.add(ustPanel, BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);

            // Doktorun randevularını göster
            doktorRandevulariniGoster();
        }

        // Çıkış butonu her iki rol için de olacak
        JPanel altPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCikis = new JButton("Çıkış");
        altPanel.add(btnCikis);
        panel.add(altPanel, BorderLayout.SOUTH);

        btnCikis.addActionListener(e -> cikisYap());

        // Paneli mainPanel'e ekle
        mainPanel.add(panel, "yonetim");
    }

    private void olusturAdminPaneli() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Üst panel
        JPanel ustPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // Admin için tüm seçenekler
        JComboBox<String> tabloSecim = new JComboBox<>(new String[]{
                "Doktorlar", "Hastalar", "Randevular", "Sistem Logları"
        });

        // Doktor ekleme butonu
        JButton btnDoktorEkle = new JButton("Yeni Doktor Ekle");
        ustPanel.add(new JLabel("Tablo: "));
        ustPanel.add(tabloSecim);
        ustPanel.add(Box.createHorizontalStrut(20));
        ustPanel.add(btnDoktorEkle);

        // Tablo
        yonetimTablosu = new JTable();
        JScrollPane scrollPane = new JScrollPane(yonetimTablosu);

        // Event handlers
        btnDoktorEkle.addActionListener(e -> doktorEkleDialogGoster());
        tabloSecim.addActionListener(e -> tabloyuGuncelle(yonetimTablosu, (String) tabloSecim.getSelectedItem()));

        // Alt panel - Çıkış butonu
        JPanel altPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCikis = new JButton("Çıkış");
        altPanel.add(btnCikis);

        btnCikis.addActionListener(e -> cikisYap());

        panel.add(ustPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(altPanel, BorderLayout.SOUTH);

        mainPanel.add(panel, "yonetim");
        tabloyuGuncelle(yonetimTablosu, "Doktorlar");
    }

    private void olusturDoktorPaneli() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Üst panel
        JPanel ustPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel baslik = new JLabel("Randevularım");
        baslik.setFont(new Font("Arial", Font.BOLD, 16));
        ustPanel.add(baslik);

        // Tablo
        yonetimTablosu = new JTable();
        JScrollPane scrollPane = new JScrollPane(yonetimTablosu);

        // Doktorun randevularını göster
        doktorRandevulariniGoster();

        // Alt panel - Çıkış butonu
        JPanel altPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCikis = new JButton("Çıkış");
        altPanel.add(btnCikis);

        btnCikis.addActionListener(e -> cikisYap());

        panel.add(ustPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(altPanel, BorderLayout.SOUTH);

        mainPanel.add(panel, "yonetim");
    }

    private void doktorRandevulariniGoster() {
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT r.id, h.tc_no, h.ad || ' ' || h.soyad as hasta_adi, " +
                            "r.tarih, r.saat, r.durum " +
                            "FROM randevular r " +
                            "JOIN hastalar h ON r.hasta_id = h.id " +
                            "JOIN doktorlar d ON r.doktor_id = d.id " +
                            "JOIN kullanicilar k ON d.kullanici_id = k.id " +
                            "WHERE k.username = ? " +
                            "ORDER BY r.tarih DESC, r.saat");
            pstmt.setString(1, currentUsername);
            ResultSet rs = pstmt.executeQuery();

            Vector<String> kolonlar = new Vector<>();
            kolonlar.add("Randevu ID");
            kolonlar.add("TC No");
            kolonlar.add("Hasta Adı");
            kolonlar.add("Tarih");
            kolonlar.add("Saat");
            kolonlar.add("Durum");

            Vector<Vector<Object>> veriler = new Vector<>();
            while(rs.next()) {
                Vector<Object> satir = new Vector<>();
                satir.add(rs.getInt("id"));
                satir.add(rs.getString("tc_no"));
                satir.add(rs.getString("hasta_adi"));
                satir.add(rs.getDate("tarih"));
                satir.add(rs.getTime("saat"));
                satir.add(rs.getString("durum"));
                veriler.add(satir);
            }

            DefaultTableModel model = new DefaultTableModel(veriler, kolonlar) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            yonetimTablosu.setModel(model);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Randevular getirilirken hata oluştu!");
        }
    }

    private void cikisYap() {
        try {
            PreparedStatement logStmt = conn.prepareStatement(
                    "SELECT log_kullanici_islem('CIKIS', ?)");
            logStmt.setString(1, currentUsername);
            logStmt.execute();

            currentUserRole = null;
            currentUsername = null;
            cardLayout.show(mainPanel, "karsilama");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Çıkış yapılırken hata oluştu!");
        }
    }
    private void doktorEkleDialogGoster() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), "Yeni Doktor Ekle", true);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Kullanıcı bilgileri
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Kullanıcı Adı:"), gbc);
        JTextField usernameField = new JTextField(15);
        gbc.gridx = 1;
        dialog.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Şifre:"), gbc);
        JPasswordField passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        dialog.add(passwordField, gbc);

        // Doktor bilgileri
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Ad:"), gbc);
        JTextField adField = new JTextField(15);
        gbc.gridx = 1;
        dialog.add(adField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Soyad:"), gbc);
        JTextField soyadField = new JTextField(15);
        gbc.gridx = 1;
        dialog.add(soyadField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("Uzmanlık:"), gbc);
        JTextField uzmanlikField = new JTextField(15);
        gbc.gridx = 1;
        dialog.add(uzmanlikField, gbc);

        // Kaydet butonu
        JButton kaydetBtn = new JButton("Kaydet");
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        dialog.add(kaydetBtn, gbc);

        kaydetBtn.addActionListener(e -> {
            try {
                // Kullanıcı oluştur
                PreparedStatement userStmt = conn.prepareStatement(
                        "INSERT INTO kullanicilar (username, password, rol) VALUES (?, ?, 'DOKTOR') RETURNING id");
                userStmt.setString(1, usernameField.getText());
                userStmt.setString(2, new String(passwordField.getPassword()));
                ResultSet userRs = userStmt.executeQuery();

                if (userRs.next()) {
                    int kullaniciId = userRs.getInt(1);

                    // Doktor oluştur
                    PreparedStatement doctorStmt = conn.prepareStatement(
                            "INSERT INTO doktorlar (kullanici_id, ad, soyad, uzmanlik) VALUES (?, ?, ?, ?)");
                    doctorStmt.setInt(1, kullaniciId);
                    doctorStmt.setString(2, adField.getText());
                    doctorStmt.setString(3, soyadField.getText());
                    doctorStmt.setString(4, uzmanlikField.getText());
                    doctorStmt.executeUpdate();

                    // Log kaydı
                    PreparedStatement logStmt = conn.prepareStatement(
                            "INSERT INTO sistem_log (islem_tipi, islem_detay, kullanici_adi) VALUES (?, ?, ?)");
                    logStmt.setString(1, "DOKTOR_EKLE");
                    logStmt.setString(2, "Yeni doktor eklendi: " + adField.getText() + " " + soyadField.getText());
                    logStmt.setString(3, currentUsername);
                    logStmt.executeUpdate();

                    JOptionPane.showMessageDialog(dialog, "Doktor başarıyla eklendi!");
                    dialog.dispose();

                    // Tabloyu güncelle
                    tabloyuGuncelle(yonetimTablosu, "Doktorlar");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Hata: " + ex.getMessage());
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(mainPanel);
        dialog.setVisible(true);
    }

    private void doktorGuncelleDialogGoster(int doktorId) {
        try {
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT d.*, k.username, k.id as kullanici_id FROM doktorlar d " +
                            "JOIN kullanicilar k ON d.kullanici_id = k.id " +
                            "WHERE d.id = ?");
            pstmt.setInt(1, doktorId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int kullaniciId = rs.getInt("kullanici_id");
                JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(mainPanel), "Doktor Güncelle", true);
                dialog.setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 5, 5, 5);
                gbc.fill = GridBagConstraints.HORIZONTAL;

                // Mevcut bilgileri form alanlarına yerleştir
                gbc.gridx = 0; gbc.gridy = 0;
                dialog.add(new JLabel("Kullanıcı Adı:"), gbc);
                JTextField usernameField = new JTextField(rs.getString("username"), 15);
                gbc.gridx = 1;
                dialog.add(usernameField, gbc);

                gbc.gridx = 0; gbc.gridy = 1;
                dialog.add(new JLabel("Ad:"), gbc);
                JTextField adField = new JTextField(rs.getString("ad"), 15);
                gbc.gridx = 1;
                dialog.add(adField, gbc);

                gbc.gridx = 0; gbc.gridy = 2;
                dialog.add(new JLabel("Soyad:"), gbc);
                JTextField soyadField = new JTextField(rs.getString("soyad"), 15);
                gbc.gridx = 1;
                dialog.add(soyadField, gbc);

                gbc.gridx = 0; gbc.gridy = 3;
                dialog.add(new JLabel("Uzmanlık:"), gbc);
                JTextField uzmanlikField = new JTextField(rs.getString("uzmanlik"), 15);
                gbc.gridx = 1;
                dialog.add(uzmanlikField, gbc);

                // Güncelle butonu
                JButton guncelleBtn = new JButton("Güncelle");
                gbc.gridx = 0; gbc.gridy = 4;
                gbc.gridwidth = 2;
                dialog.add(guncelleBtn, gbc);

                guncelleBtn.addActionListener(e -> {
                    try {
                        conn.setAutoCommit(false); // Transaction başlat

                        // Kullanıcı bilgilerini güncelle
                        PreparedStatement updateUserStmt = conn.prepareStatement(
                                "UPDATE kullanicilar SET username = ? WHERE id = ?");
                        updateUserStmt.setString(1, usernameField.getText());
                        updateUserStmt.setInt(2, kullaniciId);
                        updateUserStmt.executeUpdate();

                        // Doktor bilgilerini güncelle
                        PreparedStatement updateDoctorStmt = conn.prepareStatement(
                                "UPDATE doktorlar SET ad = ?, soyad = ?, uzmanlik = ? WHERE id = ?");
                        updateDoctorStmt.setString(1, adField.getText());
                        updateDoctorStmt.setString(2, soyadField.getText());
                        updateDoctorStmt.setString(3, uzmanlikField.getText());
                        updateDoctorStmt.setInt(4, doktorId);
                        updateDoctorStmt.executeUpdate();

                        // Log kaydı
                        PreparedStatement logStmt = conn.prepareStatement(
                                "INSERT INTO sistem_log (islem_tipi, islem_detay, kullanici_adi) VALUES (?, ?, ?)");
                        logStmt.setString(1, "DOKTOR_GUNCELLE");
                        logStmt.setString(2, "Doktor güncellendi: " + adField.getText() + " " + soyadField.getText());
                        logStmt.setString(3, currentUsername);
                        logStmt.executeUpdate();

                        conn.commit(); // Transaction'ı tamamla

                        // Önce dialog'u kapat
                        dialog.dispose();

                        // Sonra mesajı göster
                        JOptionPane.showMessageDialog(null, "Doktor bilgileri güncellendi!");

                        // En son tabloyu güncelle
                        SwingUtilities.invokeLater(() -> {
                            tabloyuGuncelle(yonetimTablosu, "Doktorlar");
                        });

                    } catch (SQLException ex) {
                        try {
                            conn.rollback();
                        } catch (SQLException rollbackEx) {
                            rollbackEx.printStackTrace();
                        }
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(dialog, "Hata: " + ex.getMessage());
                    } finally {
                        try {
                            conn.setAutoCommit(true);
                        } catch (SQLException autoCommitEx) {
                            autoCommitEx.printStackTrace();
                        }
                    }
                });

                dialog.pack();
                dialog.setLocationRelativeTo(mainPanel);
                dialog.setVisible(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Doktor bilgileri getirilirken hata oluştu!");
        }
    }
    private void tabloyuGuncelle(JTable tablo, String tabloAdi) {
        try {
            PreparedStatement pstmt;
            ResultSet rs;
            Vector<String> kolonlar = new Vector<>();
            Vector<Vector<Object>> veriler = new Vector<>();

            if ("Doktorlar".equals(tabloAdi)) {
                pstmt = conn.prepareStatement(
                        "SELECT d.id, k.username, d.ad, d.soyad, d.uzmanlik " +
                                "FROM doktorlar d " +
                                "JOIN kullanicilar k ON d.kullanici_id = k.id " +
                                "ORDER BY d.ad, d.soyad");
                rs = pstmt.executeQuery();

                kolonlar.add("ID");
                kolonlar.add("Kullanıcı Adı");
                kolonlar.add("Ad");
                kolonlar.add("Soyad");
                kolonlar.add("Uzmanlık");
                kolonlar.add("Güncelle");
                kolonlar.add("Sil");

                while(rs.next()) {
                    Vector<Object> satir = new Vector<>();
                    int doktorId = rs.getInt("id");
                    satir.add(doktorId);
                    satir.add(rs.getString("username"));
                    satir.add(rs.getString("ad"));
                    satir.add(rs.getString("soyad"));
                    satir.add(rs.getString("uzmanlik"));
                    satir.add("Güncelle");  // Buton yerine metin
                    satir.add("Sil");       // Buton yerine metin
                    veriler.add(satir);
                }

                DefaultTableModel model = new DefaultTableModel(veriler, kolonlar) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                tablo.setModel(model);

                // Güncelle ve Sil sütunları için tıklama olayı ekle
                tablo.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int row = tablo.rowAtPoint(e.getPoint());
                        int col = tablo.columnAtPoint(e.getPoint());
                        if (row >= 0) {
                            int doktorId = (int) tablo.getValueAt(row, 0);
                            if (col == 5) { // Güncelle sütunu
                                doktorGuncelleDialogGoster(doktorId);
                            } else if (col == 6) { // Sil sütunu
                                int secim = JOptionPane.showConfirmDialog(null,
                                        "Bu doktoru silmek istediğinizden emin misiniz?",
                                        "Doktor Sil",
                                        JOptionPane.YES_NO_OPTION);
                                if (secim == JOptionPane.YES_OPTION) {
                                    doktoruSil(doktorId);
                                    tabloyuGuncelle(tablo, "Doktorlar");
                                }
                            }
                        }
                    }
                });

                // Güncelle ve Sil sütunları için özel renderer
                DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                                                                   boolean isSelected, boolean hasFocus,
                                                                   int row, int column) {
                        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        if (column == 5 || column == 6) {
                            setForeground(Color.BLUE);
                            setHorizontalAlignment(JLabel.CENTER);
                            setText("<html><u>" + value + "</u></html>");
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        }
                        return c;
                    }
                };

                // Sütun genişliklerini ayarla
                tablo.getColumnModel().getColumn(5).setCellRenderer(renderer);
                tablo.getColumnModel().getColumn(6).setCellRenderer(renderer);
                tablo.getColumnModel().getColumn(5).setPreferredWidth(80);
                tablo.getColumnModel().getColumn(6).setPreferredWidth(80);

                // Satır yüksekliğini ayarla
                tablo.setRowHeight(30);
            }
            else {
                // Diğer tablolar için mevcut kodunuz aynen kalabilir
                String sql = switch (tabloAdi) {
                    case "Hastalar" ->
                            "SELECT id, tc_no, ad, soyad FROM hastalar ORDER BY ad, soyad";
                    case "Randevular" ->
                            "SELECT r.id, h.tc_no as hasta_tc, h.ad as hasta_ad, h.soyad as hasta_soyad, " +
                                    "d.ad as doktor_ad, d.soyad as doktor_soyad, r.tarih, r.saat, r.durum " +
                                    "FROM randevular r " +
                                    "JOIN hastalar h ON r.hasta_id = h.id " +
                                    "JOIN doktorlar d ON r.doktor_id = d.id " +
                                    "ORDER BY r.tarih DESC, r.saat DESC";
                    case "Sistem Logları" ->
                            "SELECT islem_tipi, islem_detay, kullanici_adi, islem_tarihi " +
                                    "FROM sistem_log ORDER BY islem_tarihi DESC";
                    default -> throw new IllegalArgumentException("Geçersiz tablo adı: " + tabloAdi);
                };

                pstmt = conn.prepareStatement(sql);
                rs = pstmt.executeQuery();

                ResultSetMetaData metaData = rs.getMetaData();
                int kolonSayisi = metaData.getColumnCount();

                for (int i = 1; i <= kolonSayisi; i++) {
                    kolonlar.add(metaData.getColumnLabel(i));
                }

                while (rs.next()) {
                    Vector<Object> satir = new Vector<>();
                    for (int i = 1; i <= kolonSayisi; i++) {
                        satir.add(rs.getObject(i));
                    }
                    veriler.add(satir);
                }
            }

            DefaultTableModel model = new DefaultTableModel(veriler, kolonlar) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int column) {
                    if (column == kolonlar.size() - 1 && "Doktorlar".equals(tabloAdi)) {
                        return JButton.class;
                    }
                    return Object.class;
                }
            };

            tablo.setModel(model);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Tablo güncellenirken hata oluştu: " + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void doktoruSil(int doktorId) {
        try {
            // Önce doktorun randevularını kontrol et
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM randevular WHERE doktor_id = ? AND durum = 'BEKLEMEDE'");
            checkStmt.setInt(1, doktorId);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(null,
                        "Bu doktorun aktif randevuları var. Önce randevuları iptal edilmeli!",
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Doktoru sil
            PreparedStatement deleteStmt = conn.prepareStatement(
                    "DELETE FROM doktorlar WHERE id = ?");
            deleteStmt.setInt(1, doktorId);
            deleteStmt.executeUpdate();

            // Log kaydı
            PreparedStatement logStmt = conn.prepareStatement(
                    "INSERT INTO sistem_log (islem_tipi, islem_detay, kullanici_adi) VALUES (?, ?, ?)");
            logStmt.setString(1, "DOKTOR_SIL");
            logStmt.setString(2, "Doktor silindi. ID: " + doktorId);
            logStmt.setString(3, currentUsername);
            logStmt.executeUpdate();

            JOptionPane.showMessageDialog(null,
                    "Doktor başarıyla silindi!",
                    "Başarılı",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Doktor silinirken hata oluştu: " + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new HastaneOtomasyonu().setVisible(true);
        });
    }
}
