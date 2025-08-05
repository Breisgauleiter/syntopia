FROM arangodb:3.11

# Copy initialization scripts
COPY initdb.d/ /docker-entrypoint-initdb.d/

# Set environment variables for automatic setup
ENV ARANGO_ROOT_PASSWORD=syntopia123
ENV ARANGO_DATABASE=syntopia

# Expose the default ArangoDB port
EXPOSE 8529

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8529/_api/version || exit 1
