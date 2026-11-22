package ua.lr11;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class Main extends JFrame {
    private final Database db;
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField nameField = new JTextField(18);
    private final JTextField countField = new JTextField(6);
    private final JTextField priceField = new JTextField(8);
    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{"Шахи", "Монополія"});
    private final JTextField extraField = new JTextField(18);
    private final JTextField filterField = new JTextField("1500", 7);

    public Main(Database db) {
        super("ЛР11 — База даних настільних ігор");
        this.db = db;
        model = new DefaultTableModel(new Object[]{"ID", "Назва", "Кількість", "Ціна (грн)", "Тип", "Додаткова інформація"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) fillFieldsFromSelection();
        });

        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 6));
        fields.setBorder(BorderFactory.createTitledBorder("Дані настільної гри"));
        fields.add(new JLabel("Назва:")); fields.add(nameField);
        fields.add(new JLabel("Кількість наборів:")); fields.add(countField);
        fields.add(new JLabel("Ціна:")); fields.add(priceField);
        fields.add(new JLabel("Тип:")); fields.add(typeBox);
        fields.add(new JLabel("Додатково:")); fields.add(extraField);

        JButton addButton = new JButton("Додати");
        JButton updateButton = new JButton("Редагувати вибраний");
        JButton deleteButton = new JButton("Видалити вибраний");
        JButton clearButton = new JButton("Очистити поля");
        JButton refreshButton = new JButton("Показати всі");
        JButton filterButton = new JButton("Фільтр: ціна менша за");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(addButton); actions.add(updateButton); actions.add(deleteButton); actions.add(clearButton);
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(refreshButton); filterPanel.add(filterButton); filterPanel.add(filterField);

        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout());
        south.add(fields, BorderLayout.NORTH);
        south.add(actions, BorderLayout.CENTER);
        south.add(filterPanel, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addRecord());
        updateButton.addActionListener(e -> updateRecord());
        deleteButton.addActionListener(e -> deleteRecord());
        clearButton.addActionListener(e -> clearFields());
        refreshButton.addActionListener(e -> refresh(false, 0));
        filterButton.addActionListener(e -> applyFilter());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        refresh(false, 0);
    }

    private void refresh(boolean filtered, double maxPrice) {
        try {
            model.setRowCount(0);
            for (Game g : db.getAll("", maxPrice, filtered)) {
                model.addRow(new Object[]{g.getId(), g.getName(), g.getCount(), g.getPrice(), g.getType(), g.getExtra()});
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void addRecord() {
        try {
            FormData d = readForm();
            db.add(d.name, d.count, d.price, d.type, d.extra);
            refresh(false, 0); clearFields();
            JOptionPane.showMessageDialog(this, "Запис додано.");
        } catch (Exception ex) { showError(ex); }
    }

    private void updateRecord() {
        int row = table.getSelectedRow();
        if (row < 0) { message("Спочатку виберіть запис у таблиці."); return; }
        try {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            FormData d = readForm();
            db.update(id, d.name, d.count, d.price, d.type, d.extra);
            refresh(false, 0); clearFields();
            message("Запис оновлено.");
        } catch (Exception ex) { showError(ex); }
    }

    private void deleteRecord() {
        int row = table.getSelectedRow();
        if (row < 0) { message("Спочатку виберіть запис у таблиці."); return; }
        int answer = JOptionPane.showConfirmDialog(this, "Видалити вибраний запис?", "Підтвердження", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            db.delete(id); refresh(false, 0); clearFields();
        } catch (SQLException ex) { showError(ex); }
    }

    private void applyFilter() {
        try {
            double max = Double.parseDouble(filterField.getText().trim());
            if (!Double.isFinite(max) || max < 0) throw new IllegalArgumentException("Ціна має бути невід'ємним числом.");
            refresh(true, max);
        } catch (Exception ex) { showError(ex); }
    }

    private FormData readForm() {
        String name = nameField.getText().trim();
        int count = Integer.parseInt(countField.getText().trim());
        double price = Double.parseDouble(priceField.getText().trim());
        String type = (String) typeBox.getSelectedItem();
        String extra = extraField.getText().trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Назва не може бути порожньою.");
        if (count <= 0) throw new IllegalArgumentException("Кількість має бути більшою за 0.");
        if (!Double.isFinite(price) || price < 0) throw new IllegalArgumentException("Ціна має бути невід'ємним числом.");
        return new FormData(name, count, price, type, extra);
    }

    private void fillFieldsFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        nameField.setText(model.getValueAt(row, 1).toString());
        countField.setText(model.getValueAt(row, 2).toString());
        priceField.setText(model.getValueAt(row, 3).toString());
        typeBox.setSelectedItem(model.getValueAt(row, 4).toString());
        extraField.setText(model.getValueAt(row, 5).toString());
    }

    private void clearFields() {
        nameField.setText(""); countField.setText(""); priceField.setText(""); extraField.setText("");
        table.clearSelection();
    }

    private void showError(Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Помилка", JOptionPane.ERROR_MESSAGE); }
    private void message(String text) { JOptionPane.showMessageDialog(this, text); }

    private static class FormData {
        String name, type, extra; int count; double price;
        FormData(String name, int count, double price, String type, String extra) {
            this.name = name; this.count = count; this.price = price; this.type = type; this.extra = extra;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { new Main(new Database()).setVisible(true); }
            catch (SQLException ex) { JOptionPane.showMessageDialog(null, "Не вдалося створити БД: " + ex.getMessage()); }
        });
    }
}
