package com.hotel.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class JsonRepository<T, ID> implements Repository<T, ID> {

    protected final Gson gson;
    protected final Path filePath;
    protected final Type listType;
    protected List<T> cache;

    protected JsonRepository(String fileName, Type listType) {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
        this.filePath = Paths.get("data", fileName);
        this.listType = listType;
        this.cache = new ArrayList<>();
        ensureDataDirectory();
        loadFromFile();
    }

    protected abstract ID getId(T entity);

    private void ensureDataDirectory() {
        try {
            Files.createDirectories(Paths.get("data"));
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục data", e);
        }
    }

    private void loadFromFile() {
        if (!Files.exists(filePath)) {
            cache = new ArrayList<>();
            saveToFile();
            return;
        }

        try (Reader reader = new InputStreamReader(
                new FileInputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
            List<T> data = gson.fromJson(reader, listType);
            cache = data != null ? data : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Lỗi đọc file: " + filePath + " - " + e.getMessage());
            cache = new ArrayList<>();
        }
    }

    protected void saveToFile() {
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(filePath.toFile()), StandardCharsets.UTF_8)) {
            gson.toJson(cache, writer);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi ghi file: " + filePath, e);
        }
    }

    @Override
    public void save(T entity) {
        cache.add(entity);
        saveToFile();
    }

    @Override
    public void saveAll(List<T> entities) {
        cache.addAll(entities);
        saveToFile();
    }

    @Override
    public Optional<T> findById(ID id) {
        return cache.stream()
                .filter(entity -> getId(entity).equals(id))
                .findFirst();
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(cache);
    }

    @Override
    public void update(T entity) {
        ID id = getId(entity);
        for (int i = 0; i < cache.size(); i++) {
            if (getId(cache.get(i)).equals(id)) {
                cache.set(i, entity);
                saveToFile();
                return;
            }
        }
    }

    @Override
    public void delete(ID id) {
        cache.removeIf(entity -> getId(entity).equals(id));
        saveToFile();
    }

    @Override
    public boolean exists(ID id) {
        return findById(id).isPresent();
    }

    @Override
    public long count() {
        return cache.size();
    }

    public void clearAndReload() {
        loadFromFile();
    }

    public void clearCache() {
        cache.clear();
        saveToFile();
    }
}
