import Link from "next/link";

export default function Home() {
  return (
    <main className="flex flex-1 flex-col justify-center px-4 py-16">
      <div className="mx-auto w-full max-w-xl">
        <p className="font-caps text-secondary">PILO</p>
        <h1 className="mt-3 font-display text-on-surface">
          Trámites administrativos, de extremo a extremo.
        </h1>
        <p className="mt-4 text-body-lg text-on-surface-variant">
          Configura requisitos, carga documentos y deja que el backend decida
          con reglas deterministas.
        </p>
        <Link
          href="/login"
          className="mt-8 inline-flex h-12 items-center rounded-md bg-primary px-5 font-label text-on-primary hover:bg-primary-hover"
        >
          Iniciar sesión
        </Link>
      </div>
    </main>
  );
}
