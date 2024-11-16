# Diretórios
SRC_DIR = src
BIN_DIR = bin

# Arquivos fontes e classes
SOURCES = $(SRC_DIR)/client/ClientInterface.java \
          $(SRC_DIR)/client/Client.java \
          $(SRC_DIR)/common/User.java \
          $(SRC_DIR)/common/AuthRequest.java \
          $(SRC_DIR)/server/Server.java

CLASSES = $(patsubst $(SRC_DIR)/%.java, $(BIN_DIR)/%.class, $(SOURCES))

# Configuração do compilador
JAVAC = javac
JFLAGS = -d $(BIN_DIR) -sourcepath $(SRC_DIR)

# Alvo padrão
all: $(CLASSES)

# Regra genérica para compilar arquivos .java
$(BIN_DIR)/%.class: $(SRC_DIR)/%.java
	@mkdir -p $(dir $@)
	$(JAVAC) $(JFLAGS) $<

# Limpeza
clean:
	rm -rf $(BIN_DIR)

# Executar o cliente
client: all
	java -cp $(BIN_DIR) client.ClientInterface

# Executar o servidor
server: all
	java -cp $(BIN_DIR) server.Server
