# Self-hosted Jitsi for Live Interviews

This setup runs a local Jitsi Meet stack with Jibri recording to local disk.

## Quick start

1) Copy the env template:

```
cp .env.example .env
```

2) Start the stack:

```
docker compose up -d
```

3) Open Jitsi at http://localhost:8000.

## Recording

- Recording files are stored under `./config/web/recordings`.
- The host in the UI can start/stop recording.

## Notes

- Update `PUBLIC_URL` and `JVB_ADVERTISE_IP` if running on another host.
- Keep `ENABLE_RECORDING=1` and `ENABLE_FILE_RECORDING_SERVICE=1` for file recording.
