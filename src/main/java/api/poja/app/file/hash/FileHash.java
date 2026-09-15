package api.poja.app.file.hash;

import api.poja.app.PojaGenerated;

@PojaGenerated
public record FileHash(FileHashAlgorithm algorithm, String value) {}
